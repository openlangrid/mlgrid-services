package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.GPULock;
import org.langrid.service.ml.Box2d;
import org.langrid.service.ml.interim.ObjectSegmentation;
import org.langrid.service.ml.interim.ObjectSegmentationResult;
import org.langrid.service.ml.interim.ObjectSegmentationService;

import jp.go.nict.langrid.commons.io.StreamUtil;
import jp.go.nict.langrid.commons.util.ArrayUtil;
import lombok.Data;

public class Detectron2ObjectSegmentationService implements ObjectSegmentationService{
	@Data
	static class Segmentation{
		private String label;
		private double conf;
		private double[] box;
	}
	@Data
	static class Result{
		private int width;
		private int height;
		private Segmentation[] results;
		private byte[][] masks;
	}

	public Detectron2ObjectSegmentationService(){
	}

	public Detectron2ObjectSegmentationService(String configName){
		this.configName = configName;
	}

	@Override
	public ObjectSegmentationResult segment(byte[] image, String imageFormat, 
			String labelLanguage) {
		try{
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var imgFile = FileUtil.createUniqueFileWithDateTime(tempDir, "req-", ".jpg");
			Files.write(imgFile.toPath(), image);
			run(configName, "temp", imgFile.getName());
			return buildResult(imgFile);
		} catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	private void run(String config, String dir, String file){
		try(var l = GPULock.acquire()){
			var cmd = String.format("PATH=$PATH:/usr/local/bin /usr/local/bin/"
					+ "docker-compose run --rm service python run.py "
					+ "--config %s --infile %s/%s", config, dir, file);
			var pb = new ProcessBuilder("bash", "-c", cmd);
			pb.directory(baseDir);
			try(var t = ServiceInvokerContext.startServiceTimer()){
				var proc = pb.start();
				try {
					proc.waitFor();
					t.close();
					var res = proc.exitValue();
					if(res != 0) {
						throw new RuntimeException(StreamUtil.readAsString(
							proc.getErrorStream(), "UTF-8"));
					}
				} finally {
					proc.destroy();
				}
			}
		} catch(RuntimeException | Error e){
			throw e;
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	private ObjectSegmentationResult buildResult(File imgFile) throws IOException{
		var resultDir = new File(imgFile + "_result");
		var outFile = new File(resultDir, "result.json");
		var res = mapper.createParser(Files.readAllBytes(outFile.toPath())
			).readValueAs(Result.class);
		byte[][] masks = new byte[res.results.length][];
		for(var i = 0; i < masks.length; i++){
			masks[i] = Files.readAllBytes(new File(
				resultDir, String.format("mask_%02d.png", i)).toPath());
		}
		var i = new AtomicInteger(0);
		return new ObjectSegmentationResult(
			res.width, res.height,
			(ObjectSegmentation[])ArrayUtil.collect(res.results, ObjectSegmentation.class,
				r->new ObjectSegmentation(
					r.getLabel(), r.getConf(),
					new Box2d(r.box[0], r.box[1], r.box[2] - r.box[0], r.box[3] - r.box[1]),
					masks[i.getAndIncrement()], "image/png"
				)
			));
	}

	private String configName = "COCO-InstanceSegmentation/mask_rcnn_R_50_FPN_3x.yaml";
	private File baseDir = new File("./procs/detectron2");
	private ObjectMapper mapper = new ObjectMapper();
}

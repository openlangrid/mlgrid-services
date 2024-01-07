package org.langrid.mlgridservices.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.service.ml.ObjectDetectionResult;
import org.langrid.service.ml.ObjectDetectionService;

import jp.go.nict.langrid.commons.io.StreamUtil;

public class YoloV5ObjectDetectionService
extends AbstractObjectDetectionService
implements ObjectDetectionService{
	public YoloV5ObjectDetectionService(){
	}
	public YoloV5ObjectDetectionService(String modelName){
		this.modelName = modelName;
	}

	@Override
	public ObjectDetectionResult detect(byte[] image, String imageFormat, 
			String labelLanguage) {
		try{
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var temp = FileUtil.createUniqueFileWithDateTime(tempDir, "image-", ".jpg");
			Files.write(temp.toPath(), image);
			return buildResult(run(modelName, temp));
		} catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public String run(String modelName, File imgFile){
		try(var l = ServiceInvokerContext.acquireGpuLock()){
			var cmd = "PATH=$PATH:/usr/local/bin /usr/local/bin/docker-compose run --rm "
					+ "yolov5 python runYoloV5.py " + modelName + " temp/" + imgFile.getName();
			var pb = new ProcessBuilder("bash", "-c", cmd);
			pb.directory(baseDir);
			return ServiceInvokerContext.exec(()->{
				var proc = pb.start();
				try {
					proc.waitFor();
					var res = proc.exitValue();
					if(res == 0) {
						var br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
						String line = null;
						while ((line = br.readLine()) != null) {
							if(!line.startsWith("Pred:")) continue;
							return line.substring("Pred: ".length());
						}
						throw new RuntimeException("no results found");
					} else {
						throw new RuntimeException(StreamUtil.readAsString(
							proc.getErrorStream(), "UTF-8"));
					}
				} finally {
					proc.destroy();
				}
			}, "execution", "docker-compose");
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private String modelName = "yolov5s";
	private File baseDir = new File("./procs/yolov5");
}

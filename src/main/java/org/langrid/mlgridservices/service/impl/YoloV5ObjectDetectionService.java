package org.langrid.mlgridservices.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.langrid.service.ml.Box2d;
import org.langrid.service.ml.ObjectDetectionResult;
import org.langrid.service.ml.ObjectDetectionService;

import jp.go.nict.langrid.commons.io.FileUtil;
import jp.go.nict.langrid.commons.util.ArrayUtil;
import lombok.Data;

public class YoloV5ObjectDetectionService implements ObjectDetectionService{
	@Data
	static class YoloResult{
		private double[] box;
		private double conf;
		private String label;
	}

	public YoloV5ObjectDetectionService(String modelName){
		this.modelName = modelName;
	}

	@Override
	public ObjectDetectionResult[] detect(String imageFormat, byte[] image,
			String labelLanguage, int maxResults) {
		try{
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var temp = FileUtil.createUniqueFile(tempDir, "image-", ".jpg");
			Files.write(temp.toPath(), image);
			var results = mapper.createParser(run(modelName, temp)).readValueAs(YoloResult[].class);
			return ArrayUtil.collect(results, ObjectDetectionResult.class, r->new ObjectDetectionResult(
					new Box2d(r.box[0], r.box[1], r.box[2] - r.box[0], r.box[3] - r.box[1]),
					r.getLabel(), r.getConf()));
		} catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public String run(String modelName, File imgFile){
		try{
			var cmd = "PATH=$PATH:/usr/local/bin /usr/local/bin/docker-compose run --rm "
					+ "yolov5 python runYoloV5.py " + modelName + " temp/" + imgFile.getName();
			var pb = new ProcessBuilder("bash", "-c", cmd);
			pb.directory(baseDir);
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
					var br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
					var lines = new StringBuilder();
					String line = null;
					while ((line = br.readLine()) != null) {
						lines.append(line);
					}
					throw new RuntimeException(lines.toString());
				}
			} finally {
				proc.destroy();
			}
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private String modelName = "yolov5s";
	private File baseDir = new File("./procs/object_detection_yolov5");
	private ObjectMapper mapper = new ObjectMapper();
}
package org.langrid.mlgridservices.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.langrid.service.ml.ImageClassificationResult;
import org.langrid.service.ml.ImageClassificationService;

import jp.go.nict.langrid.commons.io.FileUtil;

public class KerasImageClassificationService implements ImageClassificationService{
	public KerasImageClassificationService(String dockerServiceName, String modelName){
		this.dockerServiceName = dockerServiceName;
		this.modelName = modelName;
	}

	@Override
	public ImageClassificationResult[] classify(String format, byte[] image, String labelLanguage, double threshold, int maxResults) {
		try {
			var tempDir = new File("procs/image_classification_keras/temp");
			tempDir.mkdirs();
			var temp = FileUtil.createUniqueFile(tempDir, "image-", ".jpg");
			Files.write(temp.toPath(), image);
			var result = run("run" + modelName + ".py", temp);
			var ret = new ArrayList<ImageClassificationResult>();
			for(Object[] r : mapper.createParser(result).readValueAs(Object[][].class)) {
				ret.add(new ImageClassificationResult((String)r[1], (Double)r[2]));
			}
			return ret.toArray(new ImageClassificationResult[]{});
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String run(String scriptName, File imgFile){
		try{
			var cmd = "PATH=$PATH:/usr/local/bin /usr/local/bin/docker-compose run --rm "
					+ dockerServiceName + " python " + scriptName + " temp/" + imgFile.getName();
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
						if(!line.startsWith("Predicted:")) continue;
						return line.substring("Predicted: ".length());
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
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private String dockerServiceName;
	private String modelName;
	private ObjectMapper mapper = new ObjectMapper();
	private File baseDir = new File("./procs/image_classification_keras");
}

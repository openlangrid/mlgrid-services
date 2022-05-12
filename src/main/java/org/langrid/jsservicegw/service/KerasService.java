package org.langrid.jsservicegw.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;

import org.langrid.jsservicegw.controller.Request;
import org.langrid.jsservicegw.controller.Response;
import org.langrid.service.ml.ImageClassificationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.go.nict.langrid.commons.io.FileUtil;

@org.springframework.stereotype.Service
public class KerasService implements Service{
	@Override
	public Response invoke(String serviceId, Request invocation) {
		try {
			var format = (String)invocation.getArgs()[0];
			var img = Base64.getDecoder().decode((String)invocation.getArgs()[1]);
			var labelLang = (String)invocation.getArgs()[2];
			var maxResults = ((Number)invocation.getArgs()[3]).intValue();
			var tempDir = new File("procs/image_classification_keras/temp");
			tempDir.mkdirs();
			var temp = FileUtil.createUniqueFile(tempDir, "image-", ".jpg");
			Files.write(temp.toPath(), img);
			var result = run("run" + serviceId + ".py", temp);
			var ret = new ArrayList<ImageClassificationResult>();
			for(Object[] r : mapper().createParser(result).readValueAs(Object[][].class)) {
				ret.add(new ImageClassificationResult((String)r[1], (Double)r[2]));
			}
			return new Response(ret.toArray(new ImageClassificationResult[] {}));
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String run(String scriptName, File imgFile){
		try{
			var baseDir = new File("./procs/image_classification_keras");
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
	
	@Bean
	private ObjectMapper mapper() {
		return new ObjectMapper();
	}

	@Value("${jsservicegw.keras.docker-service-name}")
	private String dockerServiceName;
}

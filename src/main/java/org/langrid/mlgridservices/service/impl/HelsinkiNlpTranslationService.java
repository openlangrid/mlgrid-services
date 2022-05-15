package org.langrid.mlgridservices.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import jp.go.nict.langrid.commons.io.FileUtil;
import jp.go.nict.langrid.service_1_2.translation.TranslationService;

public class HelsinkiNlpTranslationService implements TranslationService{
	public HelsinkiNlpTranslationService(String modelName){
		this.modelName = modelName;
	}

	@Override
	public String translate(String sourceLang, String targetLang, String source) {
		try{
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var temp = FileUtil.createUniqueFile(tempDir, "source-", ".txt");
			Files.writeString(temp.toPath(), source, StandardCharsets.UTF_8);
			return run(modelName, temp);
		} catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public String run(String modelName, File file){
		try{
			var cmd = "PATH=$PATH:/usr/local/bin /usr/local/bin/docker-compose run --rm "
					+ "helsinki-nlp python run.py " + modelName + " temp/" + file.getName();
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
						if(!line.startsWith("Result:")) continue;
						return line.substring("Result: ".length());
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

	private String modelName = "opus-mt-en-jap";
	private File baseDir = new File("./procs/translation_helsinkinlp");
}

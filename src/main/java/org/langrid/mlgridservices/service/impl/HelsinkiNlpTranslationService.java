package org.langrid.mlgridservices.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.GPULock;
import org.langrid.mlgridservices.util.LanguageUtil;

import jp.go.nict.langrid.commons.io.FileUtil;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguagePairException;
import jp.go.nict.langrid.service_1_2.translation.TranslationService;

public class HelsinkiNlpTranslationService implements TranslationService{
	private String getModelName(String sourceLang, String targetLang) {
		if(LanguageUtil.matches("en", sourceLang) && LanguageUtil.matches("ja", targetLang)) {
			return "opus-mt-en-jap";
		}
		if(LanguageUtil.matches("ja", sourceLang) && LanguageUtil.matches("en", targetLang)) {
			return "opus-mt-ja-en";
		}
		return null;
	}
	@Override
	public String translate(String sourceLang, String targetLang, String source)
	throws UnsupportedLanguagePairException{
		var modelName = getModelName(sourceLang, targetLang);
		if(modelName == null) throw new UnsupportedLanguagePairException("sourceLang", "targetLang", sourceLang, targetLang);
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
		try(var l = GPULock.acquire()){
			var cmd = "PATH=$PATH:/usr/local/bin /usr/local/bin/docker-compose run --rm "
					+ "helsinki-nlp python run.py " + modelName + " temp/" + file.getName();
			var pb = new ProcessBuilder("bash", "-c", cmd);
			pb.directory(baseDir);
			try(var t = ServiceInvokerContext.startServiceTimer()){
				var proc = pb.start();
				try {
					proc.waitFor();
					t.close();
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
			}
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private File baseDir = new File("./procs/translation_helsinkinlp");
}

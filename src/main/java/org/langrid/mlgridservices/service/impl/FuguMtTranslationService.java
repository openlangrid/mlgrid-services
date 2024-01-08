package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.LanguageUtil;
import org.langrid.mlgridservices.util.ProcessUtil;
import org.langrid.service.ml.TranslationService;

import jp.go.nict.langrid.service_1_2.UnsupportedLanguagePairException;

public class FuguMtTranslationService implements TranslationService{
	public FuguMtTranslationService(){
	}

	public FuguMtTranslationService(String baseDir){
		this.baseDir = new File(baseDir);
	}

	private String getModelName(String sourceLang, String targetLang) {
		if(LanguageUtil.matches("en", sourceLang) && LanguageUtil.matches("ja", targetLang)) {
			return "staka/fugumt-en-ja";
		}
		if(LanguageUtil.matches("ja", sourceLang) && LanguageUtil.matches("en", targetLang)) {
			return "staka/fugumt-ja-en";
		}
		return null;
	}

	@Override
	public String translate(String text, String textLanguage, String targetLanguage)
	throws UnsupportedLanguagePairException{
		var modelName = getModelName(textLanguage, targetLanguage);
		if(modelName == null) throw new UnsupportedLanguagePairException(
			"textLanguage", "targetLanguage", textLanguage, targetLanguage);
		try{
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var temp = FileUtil.createUniqueFileWithDateTime(
				tempDir, "in-", ".txt");
			Files.writeString(temp.toPath(), text, StandardCharsets.UTF_8);
			return run(modelName, "temp/" + temp.getName());
		} catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public String run(String modelName, String inFilePath){
		try(var l = ServiceInvokerContext.getInstancePool().acquireAnyGpu()){
			var cmd = String.format(
				"PATH=$PATH:/usr/local/bin /usr/local/bin/docker-compose run --rm " +
				"service python run.py %s --inPath %s --outPathPrefix %2$s",
				modelName, inFilePath);
			var env = new HashMap<String, String>(){{
				put("NVIDIA_VISIBLE_DEVICES", "" + l.gpuId());
			}};
			ServiceInvokerContext.exec(()->{
				ProcessUtil.runAndWaitWithInheritingOutput(cmd, env, baseDir);
			}, "execution", "docker-compose");
			var outFile = new File(baseDir, inFilePath + ".result.txt");
			if(!outFile.exists()) return null;
			return Files.readString(outFile.toPath());
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private File baseDir = new File("./procs/fugumt");
}

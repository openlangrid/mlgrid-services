package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.ProcessUtil;
import org.langrid.service.ml.interim.ChatService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class ExternalChatService
implements ChatService{
	public ExternalChatService(){
		this.baseDir = new File("./procs/japanese-alpaca-lora");
		this.modelName = "decapoda-research/llama-7b-hf";
	}

	public ExternalChatService(String baseDir, String modelName) {
		this.baseDir = new File(baseDir);
		this.modelName = modelName;
	}

	public File getBaseDir() {
		return baseDir;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String chat(String utterance, String utteranceLanguage)
	throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException {
		try{
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var baseFile = FileUtil.createUniqueFileWithDateTime(
				tempDir, "in-", "");
			var utteranceFile = new File(baseFile.toString() + ".utterance.txt");
			Files.writeString(utteranceFile.toPath(), utterance, StandardCharsets.UTF_8);
			return run(modelName, utteranceFile, baseFile);
		} catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public String run(String modelName, File utteranceFile, File outFileBase){
		try(var l = ServiceInvokerContext.acquireGpuLock()){
			var cmd = String.format(
				"PATH=$PATH:/usr/local/bin /usr/local/bin/docker-compose run --rm " +
				"service python run.py --model %s " +
				"--utterancePath ./temp/%s " +
				"--outPathPrefix ./temp/%s",
				modelName, utteranceFile.getName(), outFileBase.getName());
			ServiceInvokerContext.exec(()->{
				ProcessUtil.runAndWait(cmd, baseDir);
			}, "execution", "docker-compose");
			var outFile = new File(baseDir, String.format("./temp/%s.result.txt", outFileBase.getName()));
			if(!outFile.exists()) return null;
			return Files.readString(outFile.toPath());
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private File baseDir;
	private String modelName;
}

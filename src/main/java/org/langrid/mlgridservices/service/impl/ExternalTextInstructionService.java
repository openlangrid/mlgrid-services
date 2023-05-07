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

public class ExternalTextInstructionService
implements TextInstructionService{
	public ExternalTextInstructionService(){
		this.baseDir = new File("./procs/japanese-alpaca-lora");
		this.modelName = "decapoda-research/llama-7b-hf";
	}

	public ExternalTextInstructionService(String baseDir, String modelName) {
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

	public String instruct(String text, String textLanguage)
	throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException {
		try{
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var baseFile = FileUtil.createUniqueFileWithDateTime(
				tempDir, "", "");
			var inputFile = new File(baseFile.toString() + ".input.txt");
			var outputFile = new File(baseFile.toString() + ".output.txt");
			Files.writeString(inputFile.toPath(), text, StandardCharsets.UTF_8);
			run(modelName, "temp", inputFile.getName(), textLanguage, outputFile.getName());
			if(!outputFile.exists()) return null;
			return Files.readString(outputFile.toPath());
		} catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public void run(String modelName, String dirName, String inputFileName, String inputLanguage, String outputFileName){
		try(var l = ServiceInvokerContext.acquireGpuLock()){
			var cmd = String.format(
				"PATH=$PATH:/usr/local/bin /usr/local/bin/docker-compose run --rm " +
				"service python instruct.py --model %1$s " +
				"--inputPath ./%2$s/%3$s " +
				"--inputLanguage %4$s " + 
				"--outPathPrefix ./%2$s/%5$s",
				modelName, dirName,
				inputFileName, inputLanguage,
				outputFileName);
			try(var t = ServiceInvokerContext.startServiceTimer()){
				ProcessUtil.runAndWait(cmd, baseDir);
			}
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private File baseDir;
	private String modelName;
}

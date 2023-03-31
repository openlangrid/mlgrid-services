package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.ProcessUtil;
import org.langrid.service.ml.interim.TextGenerationService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class ExternalTextGenerationService
implements TextGenerationService{
	public ExternalTextGenerationService(){
		this.baseDir = new File("./procs/japanese-alpaca-lora");
		this.modelName = "decapoda-research/llama-7b-hf";
	}

	public ExternalTextGenerationService(String baseDir, String modelName) {
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

	public String generate(String instruction, String input, String language)
	throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException {
		try{
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var baseFile = FileUtil.createUniqueFileWithDateTime(
				tempDir, "in-", "");
			var instFile = new File(baseFile.toString() + ".instruction.txt");
			if(instruction != null && !instruction.trim().isEmpty()) {
				Files.writeString(instFile.toPath(), instruction, StandardCharsets.UTF_8);
			}
			var inputFile = new File(baseFile.toString() + ".input.txt");
			if(input != null && !input.trim().isEmpty()) {
				Files.writeString(inputFile.toPath(), input, StandardCharsets.UTF_8);
			}
			return run(modelName, instFile, inputFile, baseFile);
		} catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public String run(String modelName, File instFile, File inputFile, File outFileBase){
		try(var l = ServiceInvokerContext.acquireGpuLock()){
			var cmd = String.format(
				"PATH=$PATH:/usr/local/bin /usr/local/bin/docker-compose run --rm " +
				"service python run.py --baseModel %s " +
				"--instructionPath ./temp/%s --inputPath ./temp/%s " +
				"--outPathPrefix ./temp/%s",
				modelName, instFile.getName(), inputFile.getName(), outFileBase.getName());
			try(var t = ServiceInvokerContext.startServiceTimer()){
				ProcessUtil.runAndWait(cmd, baseDir);
			}
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

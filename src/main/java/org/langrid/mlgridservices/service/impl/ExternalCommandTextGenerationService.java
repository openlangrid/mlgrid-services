package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.ProcessUtil;
import org.langrid.service.ml.interim.TextGenerationService;

import jp.go.nict.langrid.commons.lang.StringUtil;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class ExternalCommandTextGenerationService
implements TextGenerationService{
	public ExternalCommandTextGenerationService(
		String baseDir, String command, String modelName, String... params) {
		this.baseDir = new File(baseDir);
		this.command = command;
		this.modelName = modelName;
		this.params = params;
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

	public String generate(String text, String textLanguage)
	throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException {
		try{
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var baseFile = FileUtil.createUniqueFileWithDateTime(
				tempDir, "", "");
			var inputFile = new File(baseFile.toString() + ".input.txt");
			var outputFile = new File(baseFile.toString() + ".output.txt");
			Files.writeString(inputFile.toPath(), text, StandardCharsets.UTF_8);
			run("temp", inputFile.getName(), textLanguage, outputFile.getName());
			if(!outputFile.exists()) return null;
			return Files.readString(outputFile.toPath());
		} catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public void run(String dirName, String inputFileName, String inputLanguage, String outputFileName){
		try(var l = ServiceInvokerContext.acquireGpuLock()){
			var cmd = String.format(
				"%s " +
				"--model %s " +
				"--inputPath ./%3$s/%4$s " +
				"--inputLanguage %5$s " + 
				"--outputPath ./%3$s/%6$s ",
				command, modelName, dirName,
				inputFileName, inputLanguage, outputFileName);
			cmd += StringUtil.join(params, " ");
			var c = cmd;
			ServiceInvokerContext.exec(()->{
				ProcessUtil.runAndWaitWithInheritingOutput(c, baseDir);
			}, "execution", command);
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private File baseDir;
	private String command;
	private String modelName;
	private String[] params;
}

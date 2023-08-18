package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.ProcessUtil;
import org.langrid.service.ml.interim.MultimodalTextGenerationService;

import jp.go.nict.langrid.commons.lang.StringUtil;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class ExternalCommandMultimodalTextGenerationService
implements MultimodalTextGenerationService{
	public ExternalCommandMultimodalTextGenerationService(
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

	public String generate(String text, String textLanguage, org.langrid.service.ml.interim.File[] files)
	throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException {
		if(files.length != 1){
			throw new InvalidParameterException("files", "files must have 1 content.");
		}
		var f = files[0];
		System.out.printf("file[0] size: %d%n", f.getContent() != null ? f.getContent().length : -1);
		try{
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var baseFile = FileUtil.createUniqueFileWithDateTime(
				tempDir, "", "");
			var inputPromptFile = new File(baseFile.toString() + ".input.prompt.txt");
			var inputImageFile = new File(baseFile.toString() + ".input.image." + FileUtil.getExtFromFormat(f.getFormat()));
			var outputFile = new File(baseFile.toString() + ".output.txt");
			Files.writeString(inputPromptFile.toPath(), text, StandardCharsets.UTF_8);
			Files.write(inputImageFile.toPath(), f.getContent());
			run("temp", inputPromptFile.getName(), textLanguage,
				inputImageFile.getName(), outputFile.getName());
			if(!outputFile.exists()) return null;
			return Files.readString(outputFile.toPath());
		} catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public void run(String dirName, String inputPromptFileName, String inputPromptLanguage,
			String inputImageFileName, String outputFileName){
		try(var l = ServiceInvokerContext.acquireGpuLock()){
			var cmd = String.format(
				"%s " +
				"--model %s " +
				"--inputPromptPath ./%3$s/%4$s " +
				"--inputPromptLanguage %5$s " + 
				"--inputImagePath ./%3$s/%6$s " + 
				"--outputPath ./%3$s/%7$s ",
				command, modelName, dirName,
				inputPromptFileName, inputPromptLanguage,
				inputImageFileName,
				outputFileName);
			cmd += StringUtil.join(params, " ");
			try(var t = ServiceInvokerContext.startServiceTimer()){
				ProcessUtil.runAndWaitWithInheritingOutput(cmd, baseDir);
			}
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private File baseDir;
	private String command;
	private String modelName;
	private String[] params;
}

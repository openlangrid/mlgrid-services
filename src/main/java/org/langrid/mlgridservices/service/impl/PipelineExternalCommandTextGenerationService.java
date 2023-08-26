package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.langrid.mlgridservices.service.AbstractPipelineService;
import org.langrid.mlgridservices.service.Instance;
import org.langrid.mlgridservices.service.ProcessInstance;
import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.service.ml.interim.TextGenerationService;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.go.nict.langrid.commons.lang.StringUtil;
import jp.go.nict.langrid.commons.util.ArrayUtil;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PipelineExternalCommandTextGenerationService
extends AbstractPipelineService
implements TextGenerationService{
	public PipelineExternalCommandTextGenerationService(
		String baseDir, String command, String modelName, String... modelParams) {
		this.baseDir = new File(baseDir);
		this.command = command;
		this.modelName = modelName;
		this.modelParams = modelParams;

		this.instanceKey = String.format("%s:%s:%s:%s", baseDir, command, modelName,
			StringUtil.join(modelParams, ":"));
		this.tempDir = new File(baseDir, "temp");
		tempDir.mkdirs();
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
			var baseFile = FileUtil.createUniqueFileWithDateTime(
				tempDir, "", "");
			var inputTextFile = new File(baseFile.toString() + ".input_text.txt");
			Files.writeString(inputTextFile.toPath(), text, StandardCharsets.UTF_8);
			var inputFile = new File(baseFile.toString() + ".input.txt");
			var outputFile = new File(baseFile.toString() + ".output.txt");
			var input = m.writeValueAsString(new TextGenerationCommandInput(
				tempDir.getName() + "/" + inputTextFile.getName(),
				textLanguage,
				tempDir.getName() + "/" + outputFile.getName()
			));
			Files.writeString(inputFile.toPath(), input, StandardCharsets.UTF_8);
			var i = getInstance();
			var success = i.exec(input);
			if(success && outputFile.exists())
				return Files.readString(outputFile.toPath());
			return null;
		} catch(IOException e){
			throw new RuntimeException(e);
		} catch(InterruptedException e){
			return null;
		}
	}

	private Instance getInstance()
	throws InterruptedException{
		var instance = ServiceInvokerContext.getInstanceWithGpuLock(
			instanceKey, ()->{
				var commands = command.split(" ");
				commands = ArrayUtil.append(commands, "--model", modelName);
				commands = ArrayUtil.append(commands, modelParams);
				var pb = new ProcessBuilder(commands);
				try{
					pb.directory(baseDir);
					pb.redirectError(Redirect.INHERIT);
					return new ProcessInstance(pb.start());
				} catch(IOException e){
					throw new RuntimeException(e);
				}
		});
		return instance;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	static class TextGenerationCommandInput{
		private String textPath;
		private String textLanguage;
		private String outputPath;
	}

	private ObjectMapper m = new ObjectMapper();;

	private File baseDir;
	private String command;
	private String modelName;
	private String[] modelParams;

	private String instanceKey;
	private File tempDir;
}

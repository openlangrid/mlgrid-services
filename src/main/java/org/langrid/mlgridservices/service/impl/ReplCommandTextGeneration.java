package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.langrid.mlgridservices.service.AbstractPipelineService;
import org.langrid.mlgridservices.service.Instance;
import org.langrid.mlgridservices.service.ProcessInstance;
import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.service.ml.interim.TextGenerationService;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.go.nict.langrid.commons.lang.StringUtil;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ReplCommandTextGeneration
extends AbstractPipelineService
implements TextGenerationService{
	public ReplCommandTextGeneration(
		String basePath) {
		this.basePath = Path.of(basePath);
		this.tempDir = new File(this.basePath.toFile(), "temp");
		tempDir.mkdirs();
	}

	public ReplCommandTextGeneration(
		String basePath, String... commands) {
		this(basePath);
		setCommands(commands);
	}

	public void setCommands(String[] commands) {
		System.out.printf("setCommands(%s)%n", Arrays.toString(commands));
		this.commands = commands;
		this.instanceKey = "process:" + StringUtil.join(commands, ":");
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
				var pb = new ProcessBuilder(commands);
				try{
					pb.directory(basePath.toFile());
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

	private Path basePath;
	private String[] commands;

	private String instanceKey;
	private File tempDir;
}
package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.langrid.service.ml.interim.TextInstructionService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CmdReplTextInstruction
extends AbstractCmdRepl
implements TextInstructionService {
	public CmdReplTextInstruction(String basePath) {
		super(basePath);
	}

	public CmdReplTextInstruction(String basePath, String... commands) {
		super(basePath, commands);
	}

	@Override
	public String generate(String systemPrompt, String userPrompt, String promptLanguage)
	throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException {
		try{
			var baseFile = createBaseFile();
			var systemPromptFile = new File(baseFile.toString() + ".input_systemPrompt.txt");
			Files.writeString(systemPromptFile.toPath(), systemPrompt, StandardCharsets.UTF_8);
			var userPromptFile = new File(baseFile.toString() + ".input_userPrompt.txt");
			Files.writeString(userPromptFile.toPath(), userPrompt, StandardCharsets.UTF_8);
			var inputFile = new File(baseFile.toString() + ".input.txt");
			var outputFile = new File(baseFile.toString() + ".output.txt");
			var input = mapper().writeValueAsString(new TextInstructionCommandInput(
				getTempDir().getName() + "/" + systemPromptFile.getName(),
				getTempDir().getName() + "/" + userPromptFile.getName(),
				promptLanguage,
				getTempDir().getName() + "/" + outputFile.getName()
			));
			Files.writeString(inputFile.toPath(), input, StandardCharsets.UTF_8);
			var i = getInstance();
			var success = i.exec(input).isSucceeded();
			if(success && outputFile.exists())
				return Files.readString(outputFile.toPath());
			return null;
		} catch(IOException e){
			throw new RuntimeException(e);
		} catch(InterruptedException e){
			return null;
		}
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	static class TextInstructionCommandInput{
		private String systemPromptPath;
		private String userPromptPath;
		private String promptLanguage;
		private String outputPath;
	}
}

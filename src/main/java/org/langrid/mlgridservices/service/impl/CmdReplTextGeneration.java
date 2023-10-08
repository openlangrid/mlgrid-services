package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.langrid.service.ml.interim.TextGenerationService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CmdReplTextGeneration
extends AbstractCmdRepl
implements TextGenerationService{
	public CmdReplTextGeneration(String basePath) {
		super(basePath);
	}

	public CmdReplTextGeneration(String basePath, String... commands) {
		super(basePath, commands);
	}

	public String generate(String text, String textLanguage)
	throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException {
		try{
			var baseFile = createBaseFile();
			var inputTextFile = new File(baseFile.toString() + ".input_text.txt");
			Files.writeString(inputTextFile.toPath(), text, StandardCharsets.UTF_8);
			var inputFile = new File(baseFile.toString() + ".input.txt");
			var outputFile = new File(baseFile.toString() + ".output.txt");
			var input = mapper().writeValueAsString(new TextGenerationCommandInput(
				getTempDir().getName() + "/" + inputTextFile.getName(),
				textLanguage,
				getTempDir().getName() + "/" + outputFile.getName()
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

	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	static class TextGenerationCommandInput{
		private String textPath;
		private String textLanguage;
		private String outputPath;
	}
}

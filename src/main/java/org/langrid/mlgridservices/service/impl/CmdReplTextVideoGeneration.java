package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.langrid.service.ml.interim.TextGuidedVideoGenerationService;
import org.langrid.service.ml.interim.Video;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CmdReplTextVideoGeneration
extends AbstractCmdRepl
implements TextGuidedVideoGenerationService{
	public CmdReplTextVideoGeneration(String baseDir) {
		super(baseDir);
	}

	public CmdReplTextVideoGeneration(
		String baseDir, String... commands) {
		super(baseDir, commands);
	}

	@Override
	public Video generate(String text, String textLanguage)
			throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException {
		try{
			var baseFile = createBaseFile();
			var inputTextFile = new File(baseFile.toString() + ".input_text.txt");
			Files.writeString(inputTextFile.toPath(), text, StandardCharsets.UTF_8);
			var outputFilePrefix = baseFile.getName() + ".output";
			var inputFile = new File(baseFile.toString() + ".input.txt");
			var input = mapper().writeValueAsString(new TextVideoGenerationCommandInput(
				getTempDir().getName() + "/" + inputTextFile.getName(),
				textLanguage,
				getTempDir().getName() + "/" + outputFilePrefix
			));
			Files.writeString(inputFile.toPath(), input, StandardCharsets.UTF_8);

			var ins = getInstance();
			var success = ins.exec(input).isSucceeded();
			if(success){
				var imgFile = new File(getTempDir(), outputFilePrefix + ".mp4");
				if(imgFile.exists()){
					return new Video(
						Files.readAllBytes(imgFile.toPath()),
						"video/mp4");
				}
			}
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
	static class TextVideoGenerationCommandInput{
		private String promptPath;
		private String promptLanguage;
		private String outputPathPrefix;
	}
}

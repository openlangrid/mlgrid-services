package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

import org.langrid.service.ml.Audio;
import org.langrid.service.ml.TextToSpeechService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CmdReplTextToSpeech
extends AbstractCmdRepl
implements TextToSpeechService{
	public CmdReplTextToSpeech(String baseDir) {
		super(baseDir);
	}

	public CmdReplTextToSpeech(
		String baseDir, String... commands) {
		super(baseDir, commands);
	}

	@Override
	public Audio speak(String text, String textLanguage)
		throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException {
		try{
			var baseFile = createBaseFile();
			var inputTextFile = new File(baseFile.toString() + ".input_text.txt");
			Files.writeString(inputTextFile.toPath(), text, StandardCharsets.UTF_8);
			var outputFilePrefix = baseFile.getName() + ".output";
			var inputFile = new File(baseFile.toString() + ".input.txt");
			var input = mapper().writeValueAsString(new TextToSpeechCommandInput(
				getTempDir().getName() + "/" + inputTextFile.getName(),
				textLanguage,
				getTempDir().getName() + "/" + outputFilePrefix
			));
			Files.writeString(inputFile.toPath(), input, StandardCharsets.UTF_8);

			var ins = getInstance();
			var success = ins.exec(input).isSucceeded();
			if(success){
				var audioFile = new File(getTempDir(), outputFilePrefix + ".wav");
				if(audioFile.exists())
					return new Audio(
						Files.readAllBytes(audioFile.toPath()),
							"x-audio/wav"
						);
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
	static class TextToSpeechCommandInput{
		private String textPath;
		private String textLanguage;
		private String outputPathPrefix;
	}
}

package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

import org.langrid.service.ml.Image;
import org.langrid.service.ml.TextGuidedImageGenerationService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CmdReplTextImageGeneration
extends AbstractCmdRepl
implements TextGuidedImageGenerationService{
	public CmdReplTextImageGeneration(String baseDir) {
		super(baseDir);
	}

	public CmdReplTextImageGeneration(
		String baseDir, String... commands) {
		super(baseDir, commands);
	}

	@Override
	public Image generate(String text, String textLanguage)
			throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException {
		return generateMultiTimes(text, textLanguage, 1)[0];
	}

	@Override
	public Image[] generateMultiTimes(String text, String textLanguage, int numberOfTimes)
			throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException {
		try{
			var baseFile = createBaseFile();
			var inputTextFile = new File(baseFile.toString() + ".input_text.txt");
			Files.writeString(inputTextFile.toPath(), text, StandardCharsets.UTF_8);
			var outputFilePrefix = baseFile.getName() + ".output";
			var inputFile = new File(baseFile.toString() + ".input.txt");
			var input = mapper().writeValueAsString(new TextImageGenerationCommandInput(
				getTempDir().getName() + "/" + inputTextFile.getName(),
				textLanguage,
				numberOfTimes,
				getTempDir().getName() + "/" + outputFilePrefix
			));
			Files.writeString(inputFile.toPath(), input, StandardCharsets.UTF_8);

			var ins = getInstance();
			var success = ins.exec(input).isSucceeded();
			if(success){
				var ret = new ArrayList<Image>();
				for(var i = 0; i < numberOfTimes; i++){
					var imgFile = new File(getTempDir(), outputFilePrefix + "_" + i + ".png");
					if(!imgFile.exists()) break;
					ret.add(new Image(
						Files.readAllBytes(imgFile.toPath()),
						"image/png"
					));
				}
				return ret.toArray(new Image[]{});
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
	static class TextImageGenerationCommandInput{
		private String promptPath;
		private String promptLanguage;
		private int numberOfTimes;
		private String outputPathPrefix;
	}
}

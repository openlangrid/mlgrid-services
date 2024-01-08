package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.service.ml.interim.VisualQuestionAnsweringService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CmdReplVisualQuestionAnswering
extends AbstractCmdRepl
implements VisualQuestionAnsweringService {
	public CmdReplVisualQuestionAnswering(String baseDir) {
		super(baseDir);
	}

	public CmdReplVisualQuestionAnswering(String baseDir, String... commands) {
		super(baseDir, commands);
	}

	@Override
	public String generate(String prompt, String promptLanguage, byte[] image, String imageFormat)
			throws UnsupportedLanguageException, InvalidParameterException, ProcessFailedException {
		try{
			var baseFile = createBaseFile();
			var inputTextFile = new File(baseFile.toString() + ".input_prompt.txt");
			Files.writeString(inputTextFile.toPath(), prompt, StandardCharsets.UTF_8);
			var inputImageFile = new File(baseFile.toString() + ".input.image." + FileUtil.getExtFromFormat(imageFormat));
			Files.write(inputImageFile.toPath(), image);
			var outputFile = new File(baseFile.toString() + ".output.txt");
			var inputFile = new File(baseFile.toString() + ".input.txt");
			var input = mapper().writeValueAsString(new InstructionWithImageCommandInput(
				getTempDir().getName() + "/" + inputTextFile.getName(),
				promptLanguage,
				getTempDir().getName() + "/" + inputImageFile.getName(),
				getTempDir().getName() + "/" + outputFile.getName()
			));
			Files.writeString(inputFile.toPath(), input, StandardCharsets.UTF_8);

			var success = getInstance().exec(input).isSucceeded();
			if(success && outputFile.exists()){
				return Files.readString(outputFile.toPath());
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
	static class InstructionWithImageCommandInput{
		private String promptPath;
		private String promptLanguage;
		private String imagePath;
		private String outputPath;
	}
}

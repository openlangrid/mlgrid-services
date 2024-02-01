package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.langrid.service.ml.interim.ContextualQuestionAnsweringService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CmdReplContextualQuestionAnswering
extends AbstractCmdRepl
implements ContextualQuestionAnsweringService {
	public CmdReplContextualQuestionAnswering(String basePath) {
		super(basePath);
	}

	public CmdReplContextualQuestionAnswering(String basePath, String... commands) {
		super(basePath, commands);
	}

	@Override
	public String ask(String context, String question, String language)
	throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException {
		try{
			var baseFile = createBaseFile();
			var inputFile = new File(baseFile.toString() + ".input.txt");
			var outputFile = new File(baseFile.toString() + ".output.txt");
			var input = mapper().writeValueAsString(new ContextualQuestionAnsweringCommandInput(
				context, question, language,
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
	static class ContextualQuestionAnsweringCommandInput{
		public ContextualQuestionAnsweringCommandInput(String context, String question,
				String language, String outputPath){
			this.context = context;
			this.question = question;
			this.language = language;
			this.outputPath = outputPath;
		}
		private String serviceType = ContextualQuestionAnsweringService.class.getSimpleName();
		private String methodName = "ask";
		private String context;
		private String question;
		private String language;
		private String outputPath;
	}
}

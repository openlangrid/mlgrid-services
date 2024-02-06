package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.langrid.service.ml.interim.ChatMessage;
import org.langrid.service.ml.interim.ChatService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CmdReplChat
extends AbstractCmdRepl
implements ChatService{
	public CmdReplChat(String basePath) {
		super(basePath);
	}

	public CmdReplChat(String basePath, String... commands) {
		super(basePath, commands);
	}

	public String generate(ChatMessage[] messages)
	throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException {
		try{
			var baseFile = createBaseFile();
			var inputMessagesFile = new File(baseFile.toString() + ".input_messages.txt");
			mapper().writeValue(inputMessagesFile, messages);
			var inputFile = new File(baseFile.toString() + ".input.txt");
			var outputFile = new File(baseFile.toString() + ".output.txt");
			var input = mapper().writeValueAsString(new ChatCommandInput(
				getTempDir().getName() + "/" + inputMessagesFile.getName(),
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
	static class ChatCommandInput{
		public ChatCommandInput(String messagePath, String outputPath){
			this.messagesPath = messagePath;
			this.outputPath = outputPath;
		}
		private String serviceType = ChatService.class.getSimpleName();
		private String methodName = "generate";
		private String messagesPath;
		private String outputPath;
	}
}

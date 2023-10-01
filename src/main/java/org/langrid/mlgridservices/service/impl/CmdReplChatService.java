package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.langrid.mlgridservices.service.Instance;
import org.langrid.mlgridservices.service.ProcessInstance;
import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.ProcessUtil;
import org.langrid.service.ml.interim.ChatMessage;
import org.langrid.service.ml.interim.ChatService;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.go.nict.langrid.commons.lang.StringUtil;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ExternalChatService
implements ChatService{
	public ExternalChatService(String basePath) {
		this.basePath = Path.of(basePath);
		this.tempDir = new File(this.basePath.toFile(), "temp");
		tempDir.mkdirs();
	}

	public ExternalChatService(String basePath, String... commands) {
		this(basePath);
		setCommands(commands);
	}

	public void setCommands(String[] commands) {
		this.commands = commands;
		this.instanceKey = "process:" + StringUtil.join(commands, ":");
	}

	public void setRequiredGpuCount(int requiredGpuCount){
		this.requiredGpuCount = requiredGpuCount;
	}

	public String generate(ChatMessage[] messages)
	throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException {
		try{
			var baseFile = FileUtil.createUniqueFileWithDateTime(
				tempDir, "", "");
			var inputMessagesFile = new File(baseFile.toString() + ".input_messages.txt");
			m.writeValue(inputMessagesFile, messages);
			var inputFile = new File(baseFile.toString() + ".input.txt");
			var outputFile = new File(baseFile.toString() + ".output.txt");
			var input = m.writeValueAsString(new ChatCommandInput(
				tempDir.getName() + "/" + inputMessagesFile.getName(),
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
		var instance = ServiceInvokerContext.getInstanceWithPooledGpu(
			instanceKey, requiredGpuCount, (gpuIds)->{
				var pb = new ProcessBuilder(commands);
				if(gpuIds.length > 0){
					var ids = org.langrid.mlgridservices.util.StringUtil.join(gpuIds, v->""+v, ",");
					System.out.printf("instance(\"%s\") uses device %d%n", instanceKey, ids);
					pb.environment().put("NVIDIA_VISIBLE_DEVICES", "" + ids);
				}
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
	static class ChatCommandInput{
		private String messagesPath;
		private String outputPath;
	}

	private ObjectMapper m = new ObjectMapper();;

	private Path basePath;
	private String[] commands;
	private int requiredGpuCount = 1;

	private String instanceKey;
	private File tempDir;
}

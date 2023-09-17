package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.langrid.mlgridservices.service.Instance;
import org.langrid.mlgridservices.service.ProcessInstance;
import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.service.ml.interim.InstructionWithImageService;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.go.nict.langrid.commons.lang.StringUtil;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CmdReplInstructionWithImage
implements InstructionWithImageService {
	public CmdReplInstructionWithImage(
		String baseDir, String... commands) {
		this.baseDir = new File(baseDir);
		this.commands = commands;

		this.instanceKey = "process:" + StringUtil.join(commands, ":");
		this.tempDir = new File(baseDir, "temp");
		tempDir.mkdirs();
	}

	public void setCommands(String[] commands) {
		this.commands = commands;
		this.instanceKey = "process:" + StringUtil.join(commands, ":");
	}

	public void setRequiredGpuCount(int requiredGpuCount){
		this.requiredGpuCount = requiredGpuCount;
	}

	@Override
	public String instruct(String prompt, String promptLanguage, byte[] image, String imageFormat)
			throws UnsupportedLanguageException, InvalidParameterException, ProcessFailedException {
		try{
			var baseFile = FileUtil.createUniqueFileWithDateTime(
				tempDir, "", "");
			var inputTextFile = new File(baseFile.toString() + ".input_prompt.txt");
			Files.writeString(inputTextFile.toPath(), prompt, StandardCharsets.UTF_8);
			var inputImageFile = new File(baseFile.toString() + ".input.image." + FileUtil.getExtFromFormat(imageFormat));
			Files.write(inputImageFile.toPath(), image);
			var outputFile = new File(baseFile.toString() + ".output.txt");
			var outputFilePrefix = baseFile.getName() + ".output";
			var inputFile = new File(baseFile.toString() + ".input.txt");
			var input = m.writeValueAsString(new InstructionWithImageCommandInput(
				tempDir.getName() + "/" + inputTextFile.getName(),
				promptLanguage,
				tempDir.getName() + "/" + inputImageFile.getName(),
				tempDir.getName() + "/" + outputFilePrefix
			));
			Files.writeString(inputFile.toPath(), input, StandardCharsets.UTF_8);

			var success = getInstance().exec(input);
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
					pb.directory(baseDir);
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
	static class InstructionWithImageCommandInput{
		private String promptPath;
		private String promptLanguage;
		private String imagePath;
		private String outputPathPrefix;
	}

	private ObjectMapper m = new ObjectMapper();

	private File baseDir;
	private String[] commands;
	private int requiredGpuCount = 1;

	private String instanceKey;
	private File tempDir;
}

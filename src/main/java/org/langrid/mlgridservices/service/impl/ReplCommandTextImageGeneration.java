package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

import org.langrid.mlgridservices.service.Instance;
import org.langrid.mlgridservices.service.ProcessInstance;
import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.service.ml.Image;
import org.langrid.service.ml.TextGuidedImageGenerationService;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.go.nict.langrid.commons.lang.StringUtil;
import jp.go.nict.langrid.commons.util.ArrayUtil;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ReplCommandTextImageGeneration
implements TextGuidedImageGenerationService{
	public ReplCommandTextImageGeneration(
		String baseDir, String... commands) {
		this.baseDir = new File(baseDir);
		this.commands = commands;

		this.instanceKey = "process:" + StringUtil.join(commands, ":");
		this.tempDir = new File(baseDir, "temp");
		tempDir.mkdirs();
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
			var baseFile = FileUtil.createUniqueFileWithDateTime(
				tempDir, "", "");
			var inputTextFile = new File(baseFile.toString() + ".input_text.txt");
			Files.writeString(inputTextFile.toPath(), text, StandardCharsets.UTF_8);
			var outputFilePrefix = baseFile.getName() + ".output";
			var inputFile = new File(baseFile.toString() + ".input.txt");
			var input = m.writeValueAsString(new TextImageGenerationCommandInput(
				tempDir.getName() + "/" + inputTextFile.getName(),
				textLanguage,
				numberOfTimes,
				tempDir.getName() + "/" + outputFilePrefix
			));
			Files.writeString(inputFile.toPath(), input, StandardCharsets.UTF_8);

			var ins = getInstance();
			var success = ins.exec(input);
			if(success){
				var ret = new ArrayList<Image>();
				for(var i = 0; i < numberOfTimes; i++){
					var imgFile = new File(tempDir, outputFilePrefix + "_" + i + ".png");
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

	private Instance getInstance()
	throws InterruptedException{
		var instance = ServiceInvokerContext.getInstanceWithPooledGpu(
			instanceKey, (gpuId)->{
				String[] gpudef = {"NVIDIA_VISIBLE_DEVICES=" + gpuId};
				var pb = new ProcessBuilder(ArrayUtil.append(gpudef, commands));
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
	static class TextImageGenerationCommandInput{
		private String promptPath;
		private String promptLanguage;
		private int numberOfTimes;
		private String outputPathPrefix;
	}

	private ObjectMapper m = new ObjectMapper();

	private File baseDir;
	private String[] commands;

	private String instanceKey;
	private File tempDir;
}

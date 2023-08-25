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
import org.langrid.mlgridservices.util.ProcessUtil;
import org.langrid.service.ml.Image;
import org.langrid.service.ml.TextGuidedImageGenerationService;

import jp.go.nict.langrid.commons.lang.StringUtil;
import jp.go.nict.langrid.commons.util.ArrayUtil;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PipelineExternalCommandTextImageGenerationService
implements TextGuidedImageGenerationService{
	public PipelineExternalCommandTextImageGenerationService(
		String baseDir, String command, String modelName, String... params) {
		this.baseDir = new File(baseDir);
		this.command = command;
		this.modelName = modelName;
		this.params = params;

		this.instanceKey = String.format("%s:%s:%s:%s", baseDir, command, modelName,
			StringUtil.join(params, ":"));
		this.tempDir = new File(baseDir, "temp");
		tempDir.mkdirs();
	}

	public File getBaseDir() {
		return baseDir;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
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
			var inputFile = new File(baseFile.toString() + ".input.txt");
			var outputFilePrefix = ".output";
			Files.writeString(inputFile.toPath(), text, StandardCharsets.UTF_8);
			var ins = getInstance();
			var success = ins.exec(new TextImageGenerationCommandInput(
				tempDir.getName() + "/" + inputFile.getName(),
				textLanguage,
				numberOfTimes,
				tempDir.getName() + "/" + outputFilePrefix
			));
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
		var instance = ServiceInvokerContext.getInstanceWithGpuLock(
			instanceKey, ()->{
				var commands = command.split(" ");
				commands = ArrayUtil.append(commands, "--model", modelName);
				commands = ArrayUtil.append(commands, params);
				var pb = new ProcessBuilder(commands);
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
		private String language;
		private int numberOfTimes;
		private String outputPathPrefix;
	}
	public void run(String dirName, String inputFileName, String inputLanguage, String outputFileName){
		try(var l = ServiceInvokerContext.acquireGpuLock()){
			var cmd = String.format(
				"%s " +
				"--model %s " +
				"--inputPath ./%3$s/%4$s " +
				"--inputLanguage %5$s " + 
				"--outputPath ./%3$s/%6$s ",
				command, modelName, dirName,
				inputFileName, inputLanguage, outputFileName);
			cmd += StringUtil.join(params, " ");
			var c = cmd;
			ServiceInvokerContext.exec(()->{
				ProcessUtil.runAndWaitWithInheritingOutput(c, baseDir);
			}, "execution", command);
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private File baseDir;
	private String command;
	private String modelName;
	private String[] params;

	private String instanceKey;
	private File tempDir;
}

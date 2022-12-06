package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.GPULock;
import org.langrid.mlgridservices.util.LanguageUtil;
import org.langrid.mlgridservices.util.ProcessUtil;
import org.langrid.service.ml.TextGuidedImageConversionService;
import org.langrid.service.ml.interim.Image;
import org.langrid.service.ml.interim.TextGuidedImageGenerationService;
import org.langrid.service.ml.interim.TextGuidedImageManipulationService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class AbstractTextGuidedImageManipulationService implements TextGuidedImageManipulationService{
    private final File baseDir;
	private String modelPath;
	private String supportedLang = "en";

	public AbstractTextGuidedImageManipulationService(File baseDir){
		this.baseDir = baseDir;
	}

	public File getBaseDir() {
		return baseDir;
	}

	public String getModelPath() {
		return modelPath;
	}

	public void setModelPath(String modelPath) {
		this.modelPath = modelPath;
		if(modelPath.equals("rinna/japanese-stable-diffusion")){
			this.supportedLang = "ja";
		}
	}

	@Override
	public Image manipulate(String language, String prompt, String format, byte[] image)
			throws UnsupportedLanguageException, InvalidParameterException, ProcessFailedException {
		if(!LanguageUtil.matches(supportedLang, language))
			throw new UnsupportedLanguageException("language", language);
		try(var l = GPULock.acquire()){
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var temp = FileUtil.createUniqueFileWithDateTime(tempDir, "out-", "");
			var infile = new File(tempDir, temp.getName() + "." + FileUtil.getExtFromFormat(format));
			Files.write(infile.toPath(), image, StandardOpenOption.CREATE);
			var cmd = String.format(
					"PATH=$PATH:/usr/local/bin " +
					"/usr/local/bin/docker-compose run --rm service " +
					"python3 run.py \"%s\" %d temp/%s --modelPath \"%s\" --initImage \"temp/%s\"",
					prompt.replaceAll("\"", "\\\""), 1, temp.getName(), 
					modelPath, infile.getName());
			System.out.println(cmd);
			try(var t = ServiceInvokerContext.startServiceTimer()){
				ProcessUtil.runAndWait(cmd, baseDir);
			}
			var imgFile = new File(temp.toString() + "_0.png");
			if(!imgFile.exists()){
				throw new RuntimeException("failed to generate image.");
			}
			return new Image(
				"image/png",
				Files.readAllBytes(imgFile.toPath())
				);
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}

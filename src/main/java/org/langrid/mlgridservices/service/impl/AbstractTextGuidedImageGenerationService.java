package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.GPULock;
import org.langrid.mlgridservices.util.LanguageUtil;
import org.langrid.mlgridservices.util.ProcessUtil;
import org.langrid.service.ml.interim.Image;
import org.langrid.service.ml.interim.TextGuidedImageGenerationService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class AbstractTextGuidedImageGenerationService implements TextGuidedImageGenerationService{
    private final File baseDir;
	private String modelPath;
	private String supportedLang = "en";

	public AbstractTextGuidedImageGenerationService(File baseDir){
		this.baseDir = baseDir;
	}

	public void setModelPath(String modelPath) {
		this.modelPath = modelPath;
		if(modelPath.equals("rinna/japanese-stable-diffusion")){
			this.supportedLang = "ja";
		}
	}

	@Override
	public Image generate(String language, String text)
			throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException {
		return generateMultiTimes(language, text, 1)[0];
	}

	@Override
	public Image[] generateMultiTimes(String language, String text, int numberOfTimes)
			throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException {
		if(!LanguageUtil.matches(supportedLang, language))
			throw new UnsupportedLanguageException("language", language);
		try(var l = GPULock.acquire()){
			numberOfTimes = Math.min(numberOfTimes, 8);
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var temp = FileUtil.createUniqueFileWithDateTime(tempDir, "out-", "");
			var cmd = String.format(
					"PATH=$PATH:/usr/local/bin " +
					"/usr/local/bin/docker-compose run --rm service " +
					"python3 run.py \"%s\" %d temp/%s --modelPath \"%s\"",
					text.replaceAll("\"", "\\\""), numberOfTimes, temp.getName(),
					modelPath);
			System.out.println(cmd);
			ProcessUtil.runAndWait(cmd, baseDir);
			var ret = new ArrayList<Image>();
			for(var i = 0; i < numberOfTimes; i++){
				var imgFile = new File(temp.toString() + "_" + i + ".png");
				if(!imgFile.exists()) break;
				ret.add(new Image("image/png",
					Files.readAllBytes(imgFile.toPath())));
			}
			return ret.toArray(new Image[]{});
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
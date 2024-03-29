package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.LanguageUtil;
import org.langrid.mlgridservices.util.ProcessUtil;
import org.langrid.service.ml.Image;
import org.langrid.service.ml.TextGuidedImageGenerationService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class AbstractTextGuidedImageGenerationService implements TextGuidedImageGenerationService{
    private final File baseDir;
	private String modelPath;
	private String supportedLang = "en";
	private String additionalPrompt;
	private String scriptFile = "run.py";

	public AbstractTextGuidedImageGenerationService(File baseDir){
		this.baseDir = baseDir;
	}

	public void setModelPath(String modelPath) {
		this.modelPath = modelPath;
		if(modelPath.equals("rinna/japanese-stable-diffusion")){
			this.supportedLang = "ja";
		}
	}

	public void setAdditionalPrompt(String additionalPrompt) {
		this.additionalPrompt = additionalPrompt;
	}

	public void setScriptFile(String scriptFile) {
		this.scriptFile = scriptFile;
	}

	@Override
	public Image generate(String text, String textLanguage)
			throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException {
		return generateMultiTimes(text, textLanguage, 1)[0];
	}

	@Override
	public Image[] generateMultiTimes(String text, String textLanguage, int numberOfTimes)
			throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException {
		if(!LanguageUtil.matches(supportedLang, textLanguage))
			throw new UnsupportedLanguageException("textLanguage", textLanguage);
		try(var l = ServiceInvokerContext.getInstancePool().acquireAnyGpu()){
			if(additionalPrompt != null){
				text = additionalPrompt + ", " + text;
			}
			numberOfTimes = Math.min(numberOfTimes, 8);
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var temp = FileUtil.createUniqueFileWithDateTime(tempDir, "out-", "");
			var cmd = String.format(
					"PATH=$PATH:/usr/local/bin " +
					"/usr/local/bin/docker-compose run --rm service " +
					"python3 %s \"%s\" %d temp/%s --modelPath \"%s\"",
					scriptFile,
					text.replaceAll("\"", "\\\""), numberOfTimes, temp.getName(),
					modelPath);
			var env = new HashMap<String, String>(){{
				put("NVIDIA_VISIBLE_DEVICES", "" + l.gpuId());
			}};
			System.out.println(cmd);
			ServiceInvokerContext.exec(()->{
				ProcessUtil.runAndWait(cmd, env, baseDir);
			}, "execution", "docker-compose");
			var ret = new ArrayList<Image>();
			for(var i = 0; i < numberOfTimes; i++){
				var imgFile = new File(temp.toString() + "_" + i + ".png");
				if(!imgFile.exists()) break;
				ret.add(new Image(
					Files.readAllBytes(imgFile.toPath()),
					"image/png"
					));
			}
			return ret.toArray(new Image[]{});
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}

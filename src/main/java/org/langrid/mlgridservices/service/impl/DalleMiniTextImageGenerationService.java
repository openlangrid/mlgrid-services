package org.langrid.mlgridservices.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.LanguageUtil;
import org.langrid.service.ml.Image;
import org.langrid.service.ml.TextGuidedImageGenerationService;

import jp.go.nict.langrid.commons.io.FileUtil;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class DalleMiniTextImageGenerationService implements TextGuidedImageGenerationService{
    private File baseDir = new File("./procs/text_image_generation_dalle_mini");
	private String model;

	public DalleMiniTextImageGenerationService(){
		this.model = "dalle-mini/dalle-mini/mega-1-fp16:latest";
	}

	public DalleMiniTextImageGenerationService(String model){
		this.model = model;
	}

	@Override
	public Image[] generateMultiTimes(String text, String textLanguage, int maxResults)
			throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException {
		if(!LanguageUtil.matches("en", textLanguage))
			throw new UnsupportedLanguageException("textLanguage", textLanguage);
		try {
			maxResults = Math.min(maxResults, 8);
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var temp = FileUtil.createUniqueFile(tempDir, "outimage-", "");
			run(model, text, maxResults, "temp/" + temp.getName());
			var ret = new ArrayList<Image>();
			for(var i = 0; i < maxResults; i++){
				var imgFile = new File(tempDir, temp.getName() + "_" + i + ".jpg");
				if(!imgFile.exists()) break;
				ret.add(new Image(
					Files.readAllBytes(imgFile.toPath()),
					"image/jpeg"
				));
			}
			return ret.toArray(new Image[]{});
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void run(String model, String text, int maxResults, String outimagePrefix){
		try(var l = ServiceInvokerContext.acquireGpuLock()){
			var cmd = "PATH=$PATH:/usr/local/bin /usr/local/bin/docker-compose " +
					"run --rm dalle-mini python3 run.py " +
				model + " \"" + text.replaceAll("\"", "\\\"") + "\" " +
				maxResults + " " + outimagePrefix;
			System.out.println(cmd);
			var pb = new ProcessBuilder("bash", "-c", cmd);
			pb.directory(baseDir);
			ServiceInvokerContext.exec(()->{
				var proc = pb.start();
				try {
					proc.waitFor();
					var res = proc.exitValue();
					if(res == 0) {
						return;
					} else {
						var br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
						var lines = new StringBuilder();
						String line = null;
						while ((line = br.readLine()) != null) {
							lines.append(line);
						}
						throw new RuntimeException(lines.toString());
					}
				} finally {
					proc.destroy();
				}
			}, "execution", "docker-compose");
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}
}

package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.LanguageUtil;
import org.langrid.mlgridservices.util.ProcessUtil;
import org.langrid.service.ml.TextToImageGenerationResult;
import org.langrid.service.ml.TextToImageGenerationService;
import org.springframework.stereotype.Service;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

@Service
public class StableDiffusionTextImageGenerationService implements TextToImageGenerationService{
    private File baseDir = new File("./procs/text_image_generation_stable_diffusion");

	public StableDiffusionTextImageGenerationService(){
	}

	@Override
	public TextToImageGenerationResult[] generate(String language, String text, String imageFormat, int maxResults)
			throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException {
		if(!LanguageUtil.matches("en", language))
			throw new UnsupportedLanguageException("language", language);
		try {
			maxResults = Math.min(maxResults, 8);
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var temp = FileUtil.createUniqueFileWithDateTime(tempDir, "out-", "");
			var cmd = String.format(
					"PATH=$PATH:/usr/local/bin " +
					"/usr/local/bin/docker-compose run --rm service " +
					"python3 run.py \"%s\" %d temp/%s",
					text.replaceAll("\"", "\\\""), maxResults, temp.getName());
			System.out.println(cmd);
			ProcessUtil.runAndWait(cmd, baseDir);
			var ret = new ArrayList<TextToImageGenerationResult>();
			for(var i = 0; i < maxResults; i++){
				var imgFile = new File(temp.toString() + "_" + i + ".png");
				if(!imgFile.exists()) break;
//				var accFile = new File(tempDir, temp.getName() + "_" + i + ".acc.txt");
				ret.add(new TextToImageGenerationResult(
					Files.readAllBytes(imgFile.toPath()),
					0
//					Double.parseDouble(Files.readString(accFile.toPath()))
				));
			}
			return ret.toArray(new TextToImageGenerationResult[]{});
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}

package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.LanguageUtil;
import org.langrid.mlgridservices.util.ProcessUtil;
import org.langrid.service.ml.TextGuidedImageConversionResult;
import org.langrid.service.ml.TextGuidedImageConversionService;
import org.springframework.stereotype.Service;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

@Service
public class StableDiffusionTextGuidedImageConversionService implements TextGuidedImageConversionService{
    private File baseDir = new File("./procs/text_guided_image_conversion_stable_diffusion");

	public StableDiffusionTextGuidedImageConversionService(){
	}

	@Override
	public TextGuidedImageConversionResult convert(String language, String prompt, String format, byte[] image) 
	throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException {
		if(!LanguageUtil.matches("en", language))
			throw new UnsupportedLanguageException("language", language);
		try {
			var tempDirName = "temp";
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var workDir = FileUtil.createUniqueDirectoryWithDateTime(tempDir, "work-");
			var workDirName = workDir.getName();
			var inputFile = new File(workDir, "input." + FileUtil.getExtFromFormat(format));
			var outPath = "out";
			Files.write(inputFile.toPath(), image, StandardOpenOption.CREATE);
			var cmd = String.format(
					"PATH=$PATH:/usr/local/bin " +
					"/usr/local/bin/docker-compose run --rm service " +
					"python3 run.py \"%s\" \"%s/%s/%s\" \"%s/%s/%s\"",
					prompt.replaceAll("\"", "\\\""),
					tempDirName, workDirName, inputFile.getName(),
					tempDirName, workDirName, outPath);
			System.out.println(cmd);
			ProcessUtil.runAndWait(cmd, baseDir);
			var outFile = new File(workDir, outPath + ".png");
			if(!outFile.exists()) return new TextGuidedImageConversionResult();
			return new TextGuidedImageConversionResult(
					Files.readAllBytes(outFile.toPath())
				);
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}

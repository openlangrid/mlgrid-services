package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.GPULock;
import org.langrid.mlgridservices.util.ProcessUtil;
import org.langrid.service.ml.ImageToImageConversionResult;
import org.langrid.service.ml.ImageToImageConversionService;
import org.springframework.stereotype.Service;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;

@Service
public class CodeFormerImageToImageConversionService implements ImageToImageConversionService{
    private File baseDir = new File("./procs/image_to_image_codeformer");

	public CodeFormerImageToImageConversionService(){
	}

	@Override
	public ImageToImageConversionResult convert(String format, byte[] image)
			throws InvalidParameterException, ProcessFailedException {
		try(var l = GPULock.acquire()){
			var tempDir = new File(baseDir, "temp");
			var inputsDir = new File(tempDir, "inputs");
			inputsDir.mkdirs();
			var resultsDir = new File(tempDir, "results"); // docker-composeでマウントされる
			var inputDir = FileUtil.createUniqueDirectoryWithDateTime(inputsDir, "input-");
			var fileName = "input." + FileUtil.getExtFromFormat(format);
			Files.write(
				new File(inputDir, fileName).toPath(),
				image, StandardOpenOption.CREATE_NEW);
			var cmd = String.format(
					"PATH=$PATH:/usr/local/bin " +
					"/usr/local/bin/docker-compose run --rm service " +
					"python inference_codeformer.py --test_path /%s/%s/%s --w 0.7 --bg_upsampler realesrgan --face_upsample",
					tempDir.getName(), inputsDir.getName(), inputDir.getName());
			System.out.println(cmd);
			ProcessUtil.runAndWait(cmd, baseDir);
			var resultFile = new File(new File(new File(resultsDir, inputDir.getName() + "_0.7"), "final_results"), "input.png");
			System.out.println("result: " + resultFile);
			if(resultFile.exists()){
				return new ImageToImageConversionResult(Files.readAllBytes(resultFile.toPath()));
			}
			throw new ProcessFailedException("failed to convert image.");
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
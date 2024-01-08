package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.ProcessUtil;
import org.langrid.service.ml.Image;
import org.langrid.service.ml.ImageConversionService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;

public class RealEsrganImageToImageConversionService
implements ImageConversionService{
    private File baseDir = new File("./procs/image_to_image_real_esrgan");

	@Override
	public Image convert(byte[] image, String imageFormat)
			throws InvalidParameterException, ProcessFailedException {
		try(var l = ServiceInvokerContext.getInstancePool().acquireAnyGpu()){
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var inputFile = FileUtil.writeTempFile(tempDir, image, imageFormat);
			var cmd = String.format(
					"PATH=$PATH:/usr/local/bin " +
					"/usr/local/bin/docker-compose run --rm service " +
						"python3 inference_realesrgan.py " +
						"-i \"/work/temp/%s\" " +
						"-o \"/work/temp\" --suffix out " +
						"-n RealESRGAN_x4plus -s 4 --face_enhance",
					inputFile.getName());
			var env = new HashMap<String, String>(){{
				put("NVIDIA_VISIBLE_DEVICES", "" + l.gpuId());
			}};
			var outputFile = new File(tempDir, FileUtil.addFileNameSuffix(inputFile.getName(), "_out"));
			System.out.println(cmd);
			ServiceInvokerContext.exec(()->{
				ProcessUtil.runAndWaitWithInheritingOutput(cmd, env, baseDir);
			}, "execution", "docker-compose");
			do{
				if(outputFile.exists()) break;
				outputFile = new File(tempDir, FileUtil.changeFileExt(outputFile.getName(), ".png"));
				if(outputFile.exists()) break;
				throw new ProcessFailedException("failed to convert image.");
			} while(false);
			return new Image(
				Files.readAllBytes(outputFile.toPath()),
				"image/png");
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}

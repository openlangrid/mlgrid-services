package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.nio.file.Files;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.GPULock;
import org.langrid.mlgridservices.util.ProcessUtil;
import org.langrid.service.ml.ImageToTextConversionService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;

public class ClipInterrogatorImageToTextService
implements ImageToTextConversionService{
	private File baseDir = new File("procs/image_to_text_clip_interrogator");
	
	@Override
	public String convert(byte[] image, String imageFormat, String textLang)
	throws InvalidParameterException, ProcessFailedException {
		if(textLang.length() == 0 || !textLang.split("-")[0].equals("en")){
			throw new InvalidParameterException("textLang", "invalid textLang");
		}
		try(var l = ServiceInvokerContext.acquireGpuLock()){
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var infile = FileUtil.writeTempFile(tempDir, image, imageFormat);
			var cmd = String.format(
					"PATH=$PATH:/usr/local/bin " +
					"/usr/local/bin/docker-compose run --rm service " +
					"python3 /work/run.py --infile \"/work/temp/%s\"",
					infile.getName());
			System.out.println(cmd);
			ServiceInvokerContext.exec(()->{
				ProcessUtil.runAndWait(cmd, baseDir);
			}, "execution", "docker-compose");
			var resultFile = new File(infile.toString() + ".result.txt");
			if(!resultFile.exists()) {
				throw new ProcessFailedException("failed to generate text.");
			}
			return Files.readString(resultFile.toPath());
		} catch(RuntimeException | ProcessFailedException e) {
			throw e;
		} catch(Exception e) {
			throw new ProcessFailedException(e);
		}
	}
}

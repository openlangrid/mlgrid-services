package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

public class RealEsrganImageToImageConversionServiceTest {
	@Test
	public void test() throws Throwable{
		var s = new RealEsrganImageToImageConversionService();
		var image = Files.readAllBytes(new File("procs/image_to_image_real_esrgan/samples/04.jpg").toPath());
		s.convert(image, "image/jpeg");
	}
}

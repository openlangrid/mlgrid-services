package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

public class CodeFormerImageToImageServiceTest {
	@Test
	public void test() throws Throwable{
		var s = new CodeFormerImageToImageConversionService();
		var image = Files.readAllBytes(new File("procs/image_to_image_codeformer/inputs/04/04.jpg").toPath());
		s.convert(image, "image/jpeg");
	}
}

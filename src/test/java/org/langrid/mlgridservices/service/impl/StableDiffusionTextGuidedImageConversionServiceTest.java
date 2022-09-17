package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

public class StableDiffusionTextGuidedImageConversionServiceTest {
	@Test
	public void test() throws Throwable{
		var s = new StableDiffusionTextGuidedImageConversionService();
		s.convert("en", "A fantasy landscape, trending on artstation",
			"image/png",
			Files.readAllBytes(new File("./procs/text_guided_image_conversion_stable_diffusion/input.png").toPath()));
	}
}

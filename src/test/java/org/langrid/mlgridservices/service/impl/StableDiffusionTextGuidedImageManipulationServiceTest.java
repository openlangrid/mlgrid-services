package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

public class StableDiffusionTextGuidedImageManipulationServiceTest {
	@Test
	public void test() throws Throwable{
		var s = new StableDiffusionTextGuidedImageManipulationService();
		s.manipulate("en", "A fantasy landscape, trending on artstation",
				"image/png",
				Files.readAllBytes(new File(s.getBaseDir(), "input.png").toPath()));
	}

	@Test
	public void test_rinna_japanese_stable_diffusion() throws Throwable{
		var s = new StableDiffusionTextGuidedImageManipulationService("rinna/japanese-stable-diffusion");
		s.manipulate("ja", "A fantasy landscape, trending on artstation",
				"image/png",
				Files.readAllBytes(new File(s.getBaseDir(), "input.png").toPath()));
	}

	@Test
	public void test_trinart_waifu_diffusion_50_50() throws Throwable{
		var s = new StableDiffusionTextGuidedImageManipulationService("doohickey/trinart-waifu-diffusion-50-50");
		s.manipulate("en", "A fantasy landscape, trending on artstation",
				"image/png",
				Files.readAllBytes(new File(s.getBaseDir(), "input.png").toPath()));
	}

	@Test
	public void test_disco_diffusion() throws Throwable{
		var s = new StableDiffusionTextGuidedImageManipulationService("sd-dreambooth-library/disco-diffusion-style");
		s.manipulate("en", "A fantasy landscape, trending on artstation",
				"image/png",
				Files.readAllBytes(new File(s.getBaseDir(), "input.png").toPath()));
	}

	@Test
	public void test_waifu_diffusion() throws Throwable{
		var s = new StableDiffusionTextGuidedImageManipulationService("hakurei/waifu-diffusion");
		s.manipulate("en", "A fantasy landscape, trending on artstation",
				"image/png",
				Files.readAllBytes(new File(s.getBaseDir(), "input.png").toPath()));
	}

	@Test
	public void test_trinart_stable_diffusion_v2() throws Throwable{
		var s = new StableDiffusionTextGuidedImageManipulationService("naclbit/trinart_stable_diffusion_v2");
		s.manipulate("en", "A fantasy landscape, trending on artstation",
				"image/png",
				Files.readAllBytes(new File(s.getBaseDir(), "input.png").toPath()));
	}
}

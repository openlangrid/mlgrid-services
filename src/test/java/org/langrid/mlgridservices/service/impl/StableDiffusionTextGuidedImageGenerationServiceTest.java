package org.langrid.mlgridservices.service.impl;

import org.junit.jupiter.api.Test;

public class StableDiffusionTextGuidedImageGenerationServiceTest {
	@Test
	public void test() throws Throwable{
		var ig = new StableDiffusionTextGuidedImageGenerationService();
		ig.generate("en", "logo of an armchair in the shape of an avocado");
	}

	@Test
	public void test_rinna_japanese_stable_diffusion() throws Throwable{
		new StableDiffusionTextGuidedImageGenerationService("rinna/japanese-stable-diffusion")
			.generate("ja", "雨の金閣寺");
	}

	@Test
	public void test_trinart_waifu_diffusion_50_50() throws Throwable{
		new StableDiffusionTextGuidedImageGenerationService("doohickey/trinart-waifu-diffusion-50-50")
			.generate("en", "1girl, aqua eyes, baseball cap, blonde hair, closed mouth, earrings, green background, hat, hoop earrings, jewelry, looking at viewer, shirt, short hair, simple background, solo, upper body, yellow shirt");
	}

	@Test
	public void test_disco_diffusion() throws Throwable{
		new StableDiffusionTextGuidedImageGenerationService("sd-dreambooth-library/disco-diffusion-style")
			.generate("en", "a photo of sks toy floating on a ramen bowl");
	}

	@Test
	public void test_waifu_diffusion() throws Throwable{
		new StableDiffusionTextGuidedImageGenerationService("hakurei/waifu-diffusion")
			.generate("en", "1girl, aqua eyes, baseball cap, blonde hair, closed mouth, earrings, green background, hat, hoop earrings, jewelry, looking at viewer, shirt, short hair, simple background, solo, upper body, yellow shirt");
	}

	@Test
	public void test_trinart_stable_diffusion_v2() throws Throwable{
		new StableDiffusionTextGuidedImageGenerationService("naclbit/trinart_stable_diffusion_v2")
			.generate("en", "1girl, aqua eyes, baseball cap, blonde hair, closed mouth, earrings, green background, hat, hoop earrings, jewelry, looking at viewer, shirt, short hair, simple background, solo, upper body, yellow shirt");
	}

/*
 * "rinna/japanese-stable-diffusion",
 * "doohickey/trinart-waifu-diffusion-50-50", "sd-dreambooth-library/disco-diffusion-style"
 * "hakurei/waifu-diffusion", "naclbit/trinart_stable_diffusion_v2"(diffusers-60k, diffusers-95k / diffusers-115k)
 */
}

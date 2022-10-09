package org.langrid.mlgridservices.service.impl;

import org.junit.jupiter.api.Test;

public class WaifuDiffusionTextImageGenerationServiceTest {
	@Test
	public void test() throws Throwable{
		var ig = new WaifuDiffusionTextImageGenerationService();
		ig.generate("en", "logo of an armchair in the shape of an avocado");
	}
}

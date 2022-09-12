package org.langrid.mlgridservices.service.impl;

import org.junit.jupiter.api.Test;

public class RinnaJapaneseStableDiffusionTextImageGenerationServiceTest {
	@Test
	public void test() throws Throwable{
		var ig = new RinnaJapaneseStableDiffusionTextImageGenerationService();
		ig.generate("ja", "巨漢の男が三毛猫を追い回すアニメ", "image/png", 2);
	}
}

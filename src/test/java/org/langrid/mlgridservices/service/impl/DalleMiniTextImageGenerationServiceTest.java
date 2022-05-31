package org.langrid.mlgridservices.service.impl;

import org.junit.jupiter.api.Test;

public class DalleMiniTextImageGenerationServiceTest {
	@Test
	public void test() throws Throwable{
		var ig = new DalleMiniTextImageGenerationService();
		ig.generate("en", "logo of an armchair in the shape of an avocado", "image/jpeg", 2);
	}
}

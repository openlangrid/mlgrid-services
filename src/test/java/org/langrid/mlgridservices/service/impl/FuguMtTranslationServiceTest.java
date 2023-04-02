package org.langrid.mlgridservices.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.langrid.service.ml.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;

public class FuguMtTranslationServiceTest {
	@BeforeAll
	public void setUp() throws Throwable{
		service = new FuguMtTranslationService("./procs/fugumt");
	}

	@Test
	public void test() throws Throwable{
		var result = service.translate("en", "ja", "hello");
		System.out.println(result);
		assertNotNull(result);
		assertTrue(result.length() > 0);
	}

	@Autowired
	private TranslationService service;
}

package org.langrid.mlgridservices.services.impl;

import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.langrid.mlgridservices.service.impl.EmpathService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmpathServiceTest {
	@Test
	public void test() throws Throwable{
		System.out.println(new ObjectMapper().writeValueAsString(
				service.recognize("ja", "audio/x-wav", Files.readAllBytes(
						new File("./procs/speech_recognition_vosk/test_ja_11k.wav").toPath()
				))
			));
	}

	@Autowired
	private EmpathService service;
}

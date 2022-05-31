package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.langrid.mlgridservices.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GoogleTextToSpeechServiceTest {
	@Test
	public void test() throws Throwable{
		var ret = service.speak("ja", "こんにちは", "ja-JP-Wavenet-C", "audio/mpeg");
		var temp = new File("./temp/" + GoogleTextToSpeechServiceTest.class.getName());
		temp.mkdirs();
		var f = FileUtil.createUniqueFileWithDateTime(temp, "tts-", ".mp3");
		Files.write(f.toPath(), ret.getAudio());
	}

	@Autowired
	private GoogleTextToSpeechService service;
}

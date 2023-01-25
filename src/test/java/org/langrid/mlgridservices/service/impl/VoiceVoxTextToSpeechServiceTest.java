package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.langrid.mlgridservices.util.FileUtil;
import org.springframework.boot.test.context.SpringBootTest;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VoiceVoxTextToSpeechServiceTest {
	@Test
	public void test() throws Throwable{
		var service = new VoiceVoxTextToSpeechService(
			"./procs/voicevox_0_11_4", "0");
		var ret = service.speak("こんにちは", "ja");
		var temp = new File("./temp/" + VoiceVoxTextToSpeechServiceTest.class.getName());
		temp.mkdirs();
		var f = FileUtil.createUniqueFileWithDateTime(temp, "tts-", ".wav");
		Files.write(f.toPath(), ret.getAudio());
	}
}

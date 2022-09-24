package org.langrid.mlgridservices.service.impl;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

public class WhisperSpeechRecognitionServiceTest {
	@Test
	public void test() throws Throwable{
		var sv = new WhisperSpeechRecognitionService();
		for(var r : sv.recognize("ja", "audio/x-wav",
			Files.readAllBytes(Paths.get("./procs/speech_recognition_whisper/test_ja_16k.wav")))){
			var s = r.getStartMillis();
			var sm = s / 1000 / 60;
			var ss = (s - sm) / 1000;
			var sms = s % 1000;
			var e = r.getEndMillis();
			var em = e / 1000 / 60;
			var es = (e - em) / 1000;
			var ems = e % 1000;
			System.out.printf("[%02d:%02d.%03d ==> %02d:%02d.%03d] %s%n",
				sm, ss, sms, em, es, ems,
				r.getTranscript());
		}
	}

	@Test
	public void test2(){
		var pat = Pattern.compile("\\[(\\d+):(\\d+)\\.(\\d+) --> (\\d+):(\\d+)\\.(\\d+)\\] (.*)");
		var s = "[00:00.000 --> 00:04.000] こんにちは。音声認識のテストです。";
		System.out.println(pat.matcher(s).matches());
	}
}

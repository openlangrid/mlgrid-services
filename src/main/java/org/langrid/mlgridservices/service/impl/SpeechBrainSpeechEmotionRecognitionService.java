package org.langrid.mlgridservices.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.ProcessUtil;
import org.langrid.service.ml.EmotionRecognitionResult;
import org.langrid.service.ml.SpeechEmotionRecognitionService;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.go.nict.langrid.commons.io.StreamUtil;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class SpeechBrainSpeechEmotionRecognitionService implements SpeechEmotionRecognitionService{
    private static final File baseDir = new File("./procs/speech_emotion_recognition_speechbrain");

	@Override
	public EmotionRecognitionResult[] recognize(
		byte[] audio, String audioFormat, String audioLanguage)
			throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException
	{
		try(var l = ServiceInvokerContext.acquireGpuLock()){
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var temp = FileUtil.createUniqueFileWithDateTime(tempDir, "audio-", ".wav");
			Files.write(temp.toPath(), audio);
			tempDir.mkdirs();
			var cmd = String.format(
					"PATH=$PATH:/usr/local/bin " +
					"/usr/local/bin/docker-compose run --rm service " +
					"python3 run.py temp/%s",
					temp.getName());
			System.out.println(cmd);
			return ServiceInvokerContext.exec(()->{
				var proc = ProcessUtil.run(cmd, baseDir);
				var om = new ObjectMapper();
				EmotionRecognitionResult ret = null;
				try{
					var br = new BufferedReader(new InputStreamReader(proc.getInputStream(), "UTF-8"));
					String line = null;
					while ((line = br.readLine()) != null) {
						line = line.trim();
						if(line.length() == 0) continue;
						ret = om.readValue(line, EmotionRecognitionResult.class);
						break;
					}
					proc.waitFor();
					if(ret == null && proc.exitValue() != 0){
						throw new ProcessFailedException(StreamUtil.readAsString(proc.getErrorStream(), "UTF-8"));
					}
					return new EmotionRecognitionResult[]{ret};
				} finally{
					new File(baseDir, temp.getName()).delete();
					proc.destroy();
				}
			}, "execution", "docker-compose");
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}

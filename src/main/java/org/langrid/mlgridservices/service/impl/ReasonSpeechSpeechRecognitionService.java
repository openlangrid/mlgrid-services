package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.GPULock;
import org.langrid.mlgridservices.util.ProcessUtil;
import org.langrid.service.ml.SpeechRecognitionResult;
import org.langrid.service.ml.SpeechRecognitionService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class ReasonSpeechSpeechRecognitionService implements SpeechRecognitionService{
    private static final File baseDir = new File("./procs/reazonspeech");

	@Override
	public SpeechRecognitionResult[] recognize(byte[] audio, String audioFormat, String language)
	throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException {
		if(!language.startsWith("ja")){
			throw new UnsupportedLanguageException("language", language);
		}
		try(var l = GPULock.acquire()){
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var input = FileUtil.createUniqueFileWithDateTime(tempDir, "audio-", ".wav");
			var output = new File(input.toString() + ".out.txt");
			Files.write(input.toPath(), audio);
			tempDir.mkdirs();
			var cmd = String.format(
					"PATH=$PATH:/usr/local/bin " +
					"/usr/local/bin/docker-compose run --rm service " +
					"python run.py temp/%s",
					input.getName());
			System.out.println(cmd);
			try(var t = ServiceInvokerContext.startServiceTimer()){
				ProcessUtil.runAndWait(cmd, baseDir);
				var br = Files.newBufferedReader(output.toPath(), StandardCharsets.UTF_8);
				String line = null;
				var ret = new ArrayList<SpeechRecognitionResult>();
				while ((line = br.readLine()) != null) {
					ret.add(new SpeechRecognitionResult(-1, -1, line.trim()));
				}
				return ret.toArray(new SpeechRecognitionResult[]{});
			}
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}

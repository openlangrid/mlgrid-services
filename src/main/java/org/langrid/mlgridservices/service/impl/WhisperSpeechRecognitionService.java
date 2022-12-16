package org.langrid.mlgridservices.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.GPULock;
import org.langrid.mlgridservices.util.ProcessUtil;
import org.langrid.service.ml.interim.SpeechRecognitionResult;
import org.langrid.service.ml.interim.SpeechRecognitionService;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.go.nict.langrid.commons.io.StreamUtil;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class WhisperSpeechRecognitionService implements SpeechRecognitionService{
    private static final File baseDir = new File("./procs/speech_recognition_whisper");
	private static final Map<String, String> codeToLang = new HashMap<>();
	private static final Pattern pat = Pattern.compile("\\[(\\d+):(\\d+)\\.(\\d+) --> (\\d+):(\\d+)\\.(\\d+)\\] (.*)");

	static {
		try(var is = WhisperSpeechRecognitionService.class.getResourceAsStream(
				WhisperSpeechRecognitionService.class.getSimpleName() + "_langs.json")){
			@SuppressWarnings("unchecked")
			var langs = (Map<String, String>)new ObjectMapper().readValue(is, Map.class);
			for(var e : langs.entrySet()){
				codeToLang.put(e.getKey(), e.getValue());
			}
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public SpeechRecognitionResult[] recognize(byte[] audio, String audioFormat, String language)
	throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException {
		var lang = codeToLang.get(language);
		if(lang == null){
			throw new UnsupportedLanguageException("language", language);
		}
		lang = lang.substring(0, 1).toUpperCase() + lang.substring(1);
		try(var l = GPULock.acquire()){
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var temp = FileUtil.createUniqueFileWithDateTime(tempDir, "audio-", ".wav");
			Files.write(temp.toPath(), audio);
			tempDir.mkdirs();
			var cmd = String.format(
					"PATH=$PATH:/usr/local/bin " +
					"/usr/local/bin/docker-compose run --rm service " +
					"whisper temp/%s -o temp --language %s --model small",
					temp.getName(), lang);
			System.out.println(cmd);
			try(var t = ServiceInvokerContext.startServiceTimer()){
				var proc = ProcessUtil.run(cmd, baseDir);
				try{
					var br = new BufferedReader(new InputStreamReader(proc.getInputStream(), "UTF-8"));
					String line = null;
					var ret = new ArrayList<SpeechRecognitionResult>();
					while ((line = br.readLine()) != null) {
						var m = pat.matcher(line);
						if(!m.matches()) continue;
						ret.add(new SpeechRecognitionResult(
							Integer.parseInt(m.group(1)) * 60 * 1000 +
							Integer.parseInt(m.group(2)) * 1000 +
							Integer.parseInt(m.group(3)) * 1000,
							Integer.parseInt(m.group(4)) * 60 * 1000 +
							Integer.parseInt(m.group(5)) * 1000 +
							Integer.parseInt(m.group(6)) * 1000,
							m.group(7)));
					}
					proc.waitFor();
					if(ret.size() == 0 && proc.exitValue() != 0){
						throw new ProcessFailedException(StreamUtil.readAsString(proc.getErrorStream(), "UTF-8"));
					}
					return ret.toArray(new SpeechRecognitionResult[]{});
				} finally{
					proc.destroy();
				}
			}
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}

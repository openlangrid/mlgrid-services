package org.langrid.mlgridservices.service.impl;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.ProcessUtil;
import org.langrid.service.ml.Audio;
import org.langrid.service.ml.TextToSpeechService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

public class VoiceVoxTextToSpeechService implements TextToSpeechService{
	public VoiceVoxTextToSpeechService(String baseDir, String speakerId){
		this.baseDir = new File(baseDir);
		this.speakerId = speakerId;
	}

	@Override
	public Audio speak(String text, String textLanguage)
	throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException {
		var lang = textLanguage.toLowerCase();
		if(!lang.startsWith("ja")){
			throw new UnsupportedLanguageException("language", textLanguage);
		}
		try{
			ServiceInvokerContext.start("execution", "GPULock.acquire");
			try(var l = ServiceInvokerContext.getInstancePool().acquireAnyGpu()){
				var tempDir = new File(baseDir, "temp");
				tempDir.mkdirs();
				var tf = FileUtil.writeTempFile(tempDir, text.getBytes("UTF-8"), "text/plain");

				var cmd = String.format(
					"PATH=$PATH:/usr/local/bin " +
					"/usr/local/bin/docker-compose run --rm service " +
					"python ./example/python/run.py " +
					"--text \"%s\" " +
					"--speaker_id %s --f0_speaker_id 0 --f0_correct 0 " +
					"--root_dir_path=\"./release\" --use_gpu " +
					"--out_file_name=\"/work/%s/%s\"", 
					text, speakerId, tempDir.getName(), tf.getName() + ".wav");
				var env = new HashMap<String, String>(){{
					put("NVIDIA_VISIBLE_DEVICES", "" + l.gpuId());
				}};
				System.out.println(cmd);
				ServiceInvokerContext.exec(()->{
					ProcessUtil.runAndWaitWithInheritingOutput(cmd, env, baseDir);
				}, "execution", "docker-compose");
				return new Audio(
					Files.readAllBytes(new File(tempDir, tf.getName() + ".wav").toPath()),
					"audio/x-wav");
			} finally{
				ServiceInvokerContext.finishWithResult(null);
			}
		} catch(IOException | InterruptedException e){
			throw new ProcessFailedException(e);
		}
	}

	private File baseDir;
	private String speakerId;
}

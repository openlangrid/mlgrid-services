package org.langrid.mlgridservices.service.impl.composite;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.service.ml.Audio;
import org.langrid.service.ml.TextToSpeechService;
import org.langrid.service.ml.interim.ChatService;
import org.langrid.service.ml.interim.ChatWithTextToSpeechService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class BindedTextGenerationWithTextToSpeech
implements ChatWithTextToSpeechService {
	public BindedTextGenerationWithTextToSpeech(String cServiceName, String ttsServiceName){
		this.cServiceName = cServiceName;
		this.ttsServiceName = ttsServiceName;
	}

	@Override
	public Audio chat(String utterance, String utteranceLanguage)
			throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException {
		var si = ServiceInvokerContext.get();
		var gen = si.getService(cServiceName, ChatService.class)
				.chat(utterance, utteranceLanguage);
		return si.getService(ttsServiceName, TextToSpeechService.class)
				.speak(gen, utteranceLanguage);
	}

	private String cServiceName;
	private String ttsServiceName;
}

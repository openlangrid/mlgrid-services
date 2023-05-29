package org.langrid.mlgridservices.service.impl.composite;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.service.ml.Audio;
import org.langrid.service.ml.TextToSpeechService;
import org.langrid.service.ml.interim.TextGenerationWithTextToSpeechService;
import org.langrid.service.ml.interim.TextGenerationService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class BindedTextGenerationWithTextToSpeech
implements TextGenerationWithTextToSpeechService {
	public BindedTextGenerationWithTextToSpeech(String tiServiceName, String ttsServiceName){
		this.tiServiceName = tiServiceName;
		this.ttsServiceName = ttsServiceName;
	}

	@Override
	public Audio generate(String text, String textLanguage)
			throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException {
		var si = ServiceInvokerContext.get();
		var gen = si.getService(tiServiceName, TextGenerationService.class)
				.generate(text, textLanguage);
		return si.getService(ttsServiceName, TextToSpeechService.class)
				.speak(gen, textLanguage);
	}

	private String tiServiceName;
	private String ttsServiceName;
}

package org.langrid.mlgridservices.service.impl.composite;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.service.ml.Audio;
import org.langrid.service.ml.TextToSpeechService;
import org.langrid.service.ml.interim.TextGenerationService;
import org.langrid.service.ml.interim.TextGenerationWithTextToSpeechService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class BindedTextGenerationWithTextToSpeech
implements TextGenerationWithTextToSpeechService {
	public BindedTextGenerationWithTextToSpeech(String tgServiceName, String ttsServiceName){
		this.tgServiceName = tgServiceName;
		this.ttsServiceName = ttsServiceName;
	}

	@Override
	public Audio generate(String instruction, String input, String language)
			throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException {
		var si = ServiceInvokerContext.get();
		var gen = si.getService(tgServiceName, TextGenerationService.class)
				.generate(instruction, input, language);
		return si.getService(ttsServiceName, TextToSpeechService.class)
				.speak(gen, language);
	}

	private String tgServiceName;
	private String ttsServiceName;
}

package org.langrid.mlgridservices.service.impl;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.service.ml.SpeechRecognitionResult;
import org.langrid.service.ml.SpeechRecognitionService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class DummySpeechRecognitionService implements SpeechRecognitionService{
	@Override
	public SpeechRecognitionResult[] recognize(byte[] audio, String format, String language)
			throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException {
		try(var t = ServiceInvokerContext.startServiceTimer()){
			return new SpeechRecognitionResult[]{
				new SpeechRecognitionResult(0, 2000, "こんにちは")};
		}
	}
}

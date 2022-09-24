package org.langrid.service.ml.interim;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface SpeechRecognitionService {
	SpeechRecognitionResult[] recognize(String language, String format, byte[] audio)
	throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException;
}

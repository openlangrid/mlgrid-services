package org.langrid.service.ml;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface SpeechEmotionRecognitionService {
	SpeechEmotionRecognitionResult[] recognize(
			String language, String audioFormat, byte[] audio)
	throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException;
}

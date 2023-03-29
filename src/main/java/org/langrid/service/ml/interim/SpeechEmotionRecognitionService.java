package org.langrid.service.ml.interim;

import org.langrid.service.ml.EmotionRecognitionResult;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface SpeechEmotionRecognitionService {
	EmotionRecognitionResult[] recognize(
		byte[] audio, String audioFormat, String audioLanguage)
	throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException;
}

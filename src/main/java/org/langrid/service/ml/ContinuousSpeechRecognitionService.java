package org.langrid.service.ml;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface ContinuousSpeechRecognitionService {
	/**
	 * @return sessionId
	 */
	String startRecognition(
		String language,
		ContinuousSpeechRecognitionConfig config)
	throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException;

	ContinuousSpeechRecognitionTranscript[] processRecognition(String sessionId, byte[] audio)
	throws InvalidParameterException, ProcessFailedException;

	ContinuousSpeechRecognitionTranscript[] stopRecognition(String sessionId)
	throws InvalidParameterException, ProcessFailedException;
}

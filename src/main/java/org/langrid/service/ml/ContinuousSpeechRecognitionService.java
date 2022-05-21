package org.langrid.service.ml;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface ContinuousSpeechRecognitionService {
	String startRecognition(
		String language,
		ContinuousSpeechRecognitionConfig config,
		ContinuousSpeechRecognitionReceiverService receiver)
	throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException;

	void processRecognition(String rid, byte[] audio)
	throws InvalidParameterException, ProcessFailedException;

	void stopRecognition(String rid)
	throws InvalidParameterException, ProcessFailedException;

}

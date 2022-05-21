package org.langrid.service.ml;

import jp.go.nict.langrid.service_1_2.ProcessFailedException;

public interface ContinuousSpeechRecognitionReceiverService {
	void onRecognitionResult(
		String recognitionId,
		ContinuousSpeechRecognitionTranscript[] results);
	void onException(ProcessFailedException exception);
}

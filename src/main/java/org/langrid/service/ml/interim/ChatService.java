package org.langrid.service.ml.interim;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface ChatService {
	String chat(String utterance, String utteranceLanguage)
	throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException;
}

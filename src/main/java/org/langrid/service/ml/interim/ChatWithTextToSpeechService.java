package org.langrid.service.ml.interim;

import org.langrid.service.ml.Audio;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface ChatWithTextToSpeechService {
	Audio chat(String utterance, String utteranceLanguage)
	throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException;
}

package org.langrid.service.ml.interim;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguagePairException;

public interface TextGenerationWithTranslationService {
	String generate(String text, String textLanguage, String generationLanguage)
	throws InvalidParameterException, UnsupportedLanguagePairException, ProcessFailedException;
}

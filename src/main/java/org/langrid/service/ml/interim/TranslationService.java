package org.langrid.service.ml.interim;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguagePairException;

public interface TranslationService {
	String translate(String text, String textLanguage, String targetLanguage)
	throws InvalidParameterException, ProcessFailedException, UnsupportedLanguagePairException;
}

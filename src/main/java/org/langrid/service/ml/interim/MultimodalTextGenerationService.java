package org.langrid.service.ml.interim;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface MultimodalTextGenerationService {
	String generate(String text, String textLanguage, File[] files)
	throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException;
}

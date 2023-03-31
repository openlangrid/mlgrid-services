package org.langrid.service.ml.interim;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface TextGenerationService {
	String generate(String instruction, String input, String language)
	throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException;
}

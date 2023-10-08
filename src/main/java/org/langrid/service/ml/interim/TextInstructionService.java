package org.langrid.service.ml.interim;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface TextInstructionService {
	String generate(String systemPrompt, String userPrompt, String language)
	throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException;
}

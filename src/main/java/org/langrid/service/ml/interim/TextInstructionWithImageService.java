package org.langrid.service.ml.interim;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface TextInstructionWithImageService {
	String generate(String systemPrompt, String systemPromptLanguage,
		String userPrompt, String userPromptLanguage,
		byte[] image, String imageFormat)
	throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException;
}

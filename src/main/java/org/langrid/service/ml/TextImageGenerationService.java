package org.langrid.service.ml;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface TextImageGenerationService {
	TextImageGenerationResult[] generate(
			String language, String text, String imageFormat, int maxResults)
	throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException;
}

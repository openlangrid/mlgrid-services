package org.langrid.service.ml.interim;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface TextGuidedImageGenerationService {
	default Image generate(
			String language, String text)
	throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException{
		return generateMultiTimes(language, text, 1)[0];
	}

	Image[] generateMultiTimes(
			String language, String text, int numberOfTimes)
	throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException;
}

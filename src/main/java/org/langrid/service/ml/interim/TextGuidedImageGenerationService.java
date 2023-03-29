package org.langrid.service.ml.interim;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface TextGuidedImageGenerationService {
	default Image generate(
			String text, String textLanguage)
	throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException{
		return generateMultiTimes(text, textLanguage, 1)[0];
	}

	Image[] generateMultiTimes(
			String text, String textLanguage, int numberOfTimes)
	throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException;
}

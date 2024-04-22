package org.langrid.service.ml.interim;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface TextGuidedVideoGenerationService {
	Video generate(String text, String textLanguage)
	throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException;
}

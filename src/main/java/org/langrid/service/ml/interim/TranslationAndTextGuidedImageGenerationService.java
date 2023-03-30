package org.langrid.service.ml.interim;

import org.langrid.service.ml.Image;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguagePairException;

public interface TranslationAndTextGuidedImageGenerationService {
	Image generate(String text, String textLanguage, String targetLanguage)
	throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException, UnsupportedLanguagePairException;
}

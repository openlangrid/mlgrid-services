package org.langrid.service.ml.interim;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface ImageClassificationService {
	ImageClassificationResult[] classify(
		byte[] image, String imageFormat, String labelLanguage, int maxResults)
	throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException;
}
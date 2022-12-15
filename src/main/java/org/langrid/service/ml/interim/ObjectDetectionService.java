package org.langrid.service.ml.interim;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface ObjectDetectionService {
	ObjectDetectionResult detect(
			byte[] image, String imageFormat, String labelLanguage)
	throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException;
}

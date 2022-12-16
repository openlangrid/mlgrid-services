package org.langrid.service.ml.interim;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface TextGuidedImageManipulationService {
	Image[] manipulate(
		byte[] image, String imageFormat, String language, String prompt, int numOfGenerations
	)
	throws UnsupportedLanguageException, InvalidParameterException, ProcessFailedException;
}

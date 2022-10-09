package org.langrid.service.ml.interim;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface TextGuidedImageManipulationService {
	Image manipulate(
		String language, String prompt, String format, byte[] image
	)
	throws UnsupportedLanguageException, InvalidParameterException, ProcessFailedException;
}

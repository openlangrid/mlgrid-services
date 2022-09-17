package org.langrid.service.ml;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface TextGuidedImageConversionService {
	TextGuidedImageConversionResult convert(
		String language, String prompt, String format, byte[] image
	)
	throws UnsupportedLanguageException, InvalidParameterException, ProcessFailedException;
}

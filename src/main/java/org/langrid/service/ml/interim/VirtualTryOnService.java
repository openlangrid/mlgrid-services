package org.langrid.service.ml.interim;

import org.langrid.service.ml.Image;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface VirtualTryOnService {
	Image tryOn(byte[] humanImage, String humanImageFormat, String humanPrompt,
		byte[] garmentImage, String garmentImageFormat, String garmentPrompt,
		String garmentCateogry, String promptLanguage)
	throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException;
}

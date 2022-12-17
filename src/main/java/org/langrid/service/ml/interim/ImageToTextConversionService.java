package org.langrid.service.ml.interim;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;

public interface ImageToTextConversionService {
	String convert(byte[] image, String imageFormat, String textLang)
	throws InvalidParameterException, ProcessFailedException;
}

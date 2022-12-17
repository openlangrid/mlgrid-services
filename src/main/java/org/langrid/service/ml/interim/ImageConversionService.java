package org.langrid.service.ml.interim;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;

public interface ImageConversionService {
	Image convert(byte[] image, String imageFormat)
	throws InvalidParameterException, ProcessFailedException;
}

package org.langrid.service.ml;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;

public interface ImageToImageConversionService {
	ImageToImageConversionResult[] convert(
		String format, byte[] image, int maxResults
	)
	throws InvalidParameterException, ProcessFailedException;
}

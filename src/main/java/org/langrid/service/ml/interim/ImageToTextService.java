package org.langrid.service.ml.interim;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;

public interface ImageToTextService {
	String generate(String format, byte[] image)
	throws InvalidParameterException, ProcessFailedException;
}

package org.langrid.service.ml.interim;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface VisualQuestionAnsweringService {
	String generate(String question, String questionLanguage, byte[] image, String imageFormat)
	throws UnsupportedLanguageException, InvalidParameterException, ProcessFailedException;
}

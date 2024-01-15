package org.langrid.service.ml.interim;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface ContextualQuestionAnsweringService {
	String ask(String context, String question, String language)
	throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException;
}

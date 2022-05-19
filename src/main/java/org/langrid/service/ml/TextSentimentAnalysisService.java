package org.langrid.service.ml;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public interface TextSentimentAnalysisService {
    TextSentimentAnalysisResult analyze(String language, String text)
    throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException;
}

package org.langrid.service.ml.interim;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguagePairException;

public interface TextSimilarityCalculationService {
	double calculate(String text1, String text1Langauge,
		String text2, String text2Language)
		throws InvalidParameterException, UnsupportedLanguagePairException, ProcessFailedException;	
}

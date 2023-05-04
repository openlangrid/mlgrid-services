package org.langrid.mlgridservices.service.impl;

import org.langrid.service.ml.interim.TextSimilarityCalculationService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguagePairException;

public class USETextSimilarityCalculation
implements TextSimilarityCalculationService{
	@Override
	public double calculate(String text1, String text1Langauge, String text2, String text2Language)
			throws InvalidParameterException, UnsupportedLanguagePairException, ProcessFailedException {
		// TODO Auto-generated method stub
		return 0;
	}
}

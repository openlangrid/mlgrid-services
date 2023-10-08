package org.langrid.mlgridservices.service.impl.test;

import org.langrid.service.ml.interim.VisualQuestionAnsweringService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class TestVisualQuestionAnswering
implements VisualQuestionAnsweringService{
	public TestVisualQuestionAnswering(String baseDir){

	}
	@Override
	public String generate(String prompt, String promptLanguage, byte[] image, String imageFormat)
			throws UnsupportedLanguageException, InvalidParameterException, ProcessFailedException {
		return "hello";
	}
}

package org.langrid.mlgridservices.service.impl.test;

import org.langrid.service.ml.interim.InstructionWithImageService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class TestInstructionWithImage
implements InstructionWithImageService{
	public TestInstructionWithImage(String baseDir){

	}
	@Override
	public String instruct(String prompt, String promptLanguage, byte[] image, String imageFormat)
			throws UnsupportedLanguageException, InvalidParameterException, ProcessFailedException {
		return "hello";
	}
}

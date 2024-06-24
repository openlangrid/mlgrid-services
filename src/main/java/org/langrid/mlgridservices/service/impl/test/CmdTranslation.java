package org.langrid.mlgridservices.service.impl.test;

import java.io.IOException;

import org.langrid.mlgridservices.service.impl.AbstractCmdRepl;
import org.langrid.service.ml.TranslationService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguagePairException;

public class CmdTranslation extends AbstractCmdRepl implements TranslationService{
	public CmdTranslation(String basePath) {
		super(basePath);
	}

	public CmdTranslation(String basePath, String... commands) {
		super(basePath, commands);
	}

	@Override
	public String translate(String text, String textLanguage, String targetLanguage)
			throws InvalidParameterException, ProcessFailedException, UnsupportedLanguagePairException {
		try{
			getInstance().exec("text");
		} catch(InterruptedException | IOException e){
			e.printStackTrace();
		}
		return null;
	}
}

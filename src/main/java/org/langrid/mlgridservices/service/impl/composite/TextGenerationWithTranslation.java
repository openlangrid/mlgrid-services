package org.langrid.mlgridservices.service.impl.composite;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.service.ml.TranslationService;
import org.langrid.service.ml.interim.TextGenerationService;
import org.langrid.service.ml.interim.TextGenerationWithTranslationService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguagePairException;

public class TextGenerationWithTranslation
implements TextGenerationWithTranslationService{
	@Override
	public String generate(String text, String textLanguage, String generationLanguage)
			throws InvalidParameterException, UnsupportedLanguagePairException, ProcessFailedException {
		var si = ServiceInvokerContext.get();
		var trans = si.getBindedService("Pretranslation", TranslationService.class)
				.translate(text, textLanguage, generationLanguage);
		var gen = si.getBindedService("TextGeneration", TextGenerationService.class)
				.generate(trans, generationLanguage);
		var ret = si.getBindedService("Posttranslation", TranslationService.class)
				.translate(gen, generationLanguage, textLanguage);
		return ret;
	}
}

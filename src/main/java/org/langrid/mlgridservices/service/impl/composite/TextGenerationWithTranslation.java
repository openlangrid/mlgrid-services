package org.langrid.mlgridservices.service.impl.composite;

import org.langrid.mlgridservices.service.CompositeService;
import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.service.ml.TranslationService;
import org.langrid.service.ml.interim.TextGenerationService;
import org.langrid.service.ml.interim.TextGenerationWithTranslationService;
import org.langrid.service.ml.interim.management.ServiceInvocation;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguagePairException;

public class TextGenerationWithTranslation
implements TextGenerationWithTranslationService, CompositeService{
	@Override
	public String generate(String text, String textLanguage, String generationLanguage)
			throws InvalidParameterException, UnsupportedLanguagePairException, ProcessFailedException {
		var ctx = ServiceInvokerContext.current();
		var trans = ctx.getBindedService("Pretranslation", TranslationService.class)
				.translate(text, textLanguage, generationLanguage);
		var gen = ctx.getBindedService("TextGeneration", TextGenerationService.class)
				.generate(trans, generationLanguage);
		var ret = ctx.getBindedService("Posttranslation", TranslationService.class)
				.translate(gen, generationLanguage, textLanguage);
		return ret;
	}

	@Override
	public ServiceInvocation[] getInvocations() {
		return new ServiceInvocation[]{
			new ServiceInvocation("Pretranslation", "TranslationService"),
			new ServiceInvocation("TextGeneration", "TextGenerationService"),
			new ServiceInvocation("Posttranslation", "TranslationService")			
		};
	}
}

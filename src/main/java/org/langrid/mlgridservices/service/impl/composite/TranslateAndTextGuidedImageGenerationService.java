package org.langrid.mlgridservices.service.impl.composite;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.service.ml.TextGuidedImageGenerationService;
import org.langrid.service.ml.Image;
import org.langrid.service.ml.TranslationService;
import org.langrid.service.ml.interim.TextGuidedImageGenerationWithTranslationService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguagePairException;

public class TranslateAndTextGuidedImageGenerationService
implements TextGuidedImageGenerationWithTranslationService{
	public Image generate(String text, String textLanguage, String targetLanguage)
		throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException, UnsupportedLanguagePairException{
		var sc = ServiceInvokerContext.get();
		var transResult = sc.getBindedService("Translation", TranslationService.class)
			.translate(text, textLanguage, targetLanguage);
		var genResult = sc.getBindedService("TextGuidedImageGeneration", TextGuidedImageGenerationService.class)
			.generate(transResult, targetLanguage);
		return genResult;
	}
}

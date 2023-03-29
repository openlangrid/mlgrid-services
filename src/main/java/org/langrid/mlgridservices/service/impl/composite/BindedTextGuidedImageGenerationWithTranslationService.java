package org.langrid.mlgridservices.service.impl.composite;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.service.ml.interim.Image;
import org.langrid.service.ml.interim.TextGuidedImageGenerationService;
import org.langrid.service.ml.interim.TranslationService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;


public class BindedTextGuidedImageGenerationWithTranslationService
implements TextGuidedImageGenerationService{
	public BindedTextGuidedImageGenerationWithTranslationService(
		String targetLanguage, String transServiceName, String tgigServiceName
	){
		this.targetLanguage = targetLanguage;
		this.transServiceName = transServiceName;
		this.tgigServiceName = tgigServiceName;
	}

	public Image[] generateMultiTimes(
			String text, String textLanguage, int numberOfTimes)
	throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException{
		var sc = ServiceInvokerContext.get();
		var trans = sc.getService(transServiceName, TranslationService.class)
			.translate(text, textLanguage, targetLanguage);
		return sc.getService(tgigServiceName, TextGuidedImageGenerationService.class)
			.generateMultiTimes(trans, targetLanguage, numberOfTimes);
	}

	private String targetLanguage;
	private String transServiceName;
	private String tgigServiceName;
}

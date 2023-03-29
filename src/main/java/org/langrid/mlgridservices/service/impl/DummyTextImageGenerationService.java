package org.langrid.mlgridservices.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.service.ml.TextToImageGenerationResult;
import org.langrid.service.ml.TextToImageGenerationService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class DummyTextImageGenerationService implements TextToImageGenerationService{
	@Override
	public TextToImageGenerationResult[] generate(String text, String textLanguage, String imageFormat, int maxResults)
			throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException {
		try(var t = ServiceInvokerContext.startServiceTimer()){
			return new TextToImageGenerationResult[]{
					new TextToImageGenerationResult(
							Files.readAllBytes(Path.of("./procs/text_image_generation_dalle_mini/dummy.jpg")),
							1.0)};
		} catch (IOException e) {
			throw new ProcessFailedException(e);
		}
	}
}

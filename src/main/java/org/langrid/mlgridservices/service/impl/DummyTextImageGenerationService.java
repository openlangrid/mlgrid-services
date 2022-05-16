package org.langrid.mlgridservices.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.langrid.service.ml.TextImageGenerationService;
import org.langrid.service.ml.TextImageGenerationResult;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class DummyTextImageGenerationService implements TextImageGenerationService{
    @Override
    public TextImageGenerationResult[] generate(String language, String text, String imageFormat, int maxResults)
            throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException {
        try {
            return new TextImageGenerationResult[]{
                new TextImageGenerationResult(
                    Files.readAllBytes(Path.of("./procs/text_image_generation_dalle_mini/dummy.jpg")),
                    1.0)};
        } catch (IOException e) {
            throw new ProcessFailedException(e);
        }
    }
}

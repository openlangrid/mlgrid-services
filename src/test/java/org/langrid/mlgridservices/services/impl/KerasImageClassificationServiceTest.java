package org.langrid.mlgridservices.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.langrid.mlgridservices.service.impl.KerasImageClassificationService;

public class KerasImageClassificationServiceTest {
    @Test
    public void vgg19() throws Throwable{
        var ic = new KerasImageClassificationService("keras-cpu", "VGG19");
        var ret = ic.classify("image/jpeg",
            Files.readAllBytes(Path.of("./procs/image_classification_keras/cat.jpg")),
            "ja", 10);
        assertEquals(3, ret.length);
    }
}

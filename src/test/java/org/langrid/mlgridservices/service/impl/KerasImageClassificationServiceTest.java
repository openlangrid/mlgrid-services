package org.langrid.mlgridservices.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

public class KerasImageClassificationServiceTest {
    @Test
    public void vgg19() throws Throwable{
        var ic = new KerasImageClassificationService("keras-cpu", "VGG19");
        var ret = ic.classify(
            Files.readAllBytes(Path.of("./procs/image_classification_keras/cat.jpg")),
            "image/jpeg",
            "ja", 10);
        System.out.println(new ObjectMapper().writeValueAsString(ret));
        assertEquals(3, ret.length);
    }
}

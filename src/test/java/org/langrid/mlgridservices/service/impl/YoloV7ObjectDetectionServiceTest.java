package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

public class YoloV7ObjectDetectionServiceTest {
	@Test
	public void test() throws Throwable{
		var s = new YoloV7ObjectDetectionService("yolov7-e6e.pt");
		var image = Files.readAllBytes(new File("procs/yolov7/sample/zidane.jpg").toPath());
		s.detect(image, "image/jpeg","en");
	}
}

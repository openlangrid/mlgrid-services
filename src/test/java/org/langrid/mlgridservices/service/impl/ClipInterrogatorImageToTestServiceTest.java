package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

public class ClipInterrogatorImageToTestServiceTest {
	@Test
	public void test() throws Throwable{
		var s = new ClipInterrogatorImageToTextService();
		var image = Files.readAllBytes(new File(
			"procs/image_to_text_interrogator/sample/ignacio-bazan-lazcano-book-4-final.jpeg").toPath());
		System.out.println(s.generate("image/jpeg", image));
	}
}

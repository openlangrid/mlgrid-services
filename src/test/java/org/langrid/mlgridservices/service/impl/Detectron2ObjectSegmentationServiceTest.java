package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

public class Detectron2ObjectSegmentationServiceTest {
	@Test
	public void test() throws Throwable{
		var s = new Detectron2ObjectSegmentationService();
		var image = Files.readAllBytes(new File("procs/detectron2/sample/input.jpg").toPath());
		s.segment(image, "image/jpeg", "en");
	}

	@Test
	public void test2() throws Throwable{
		var s = new Detectron2ObjectSegmentationService(
//			"new_baselines/mask_rcnn_regnety_4gf_dds_FPN_400ep_LSJ.py"
			"LVISv0.5-InstanceSegmentation/mask_rcnn_R_50_FPN_1x.yaml"
		);
		var image = Files.readAllBytes(new File("procs/detectron2/sample/input.jpg").toPath());
		s.segment(image, "image/jpeg", "en");
	}
}

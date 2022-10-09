package org.langrid.mlgridservices.service.impl;

import java.io.File;

import org.springframework.stereotype.Service;

@Service
public class WaifuDiffusionTextImageGenerationService extends AbstractTextGuidedImageGenerationService{
	public WaifuDiffusionTextImageGenerationService(){
		super(new File("./procs/text_image_generation_waifu_diffusion"));
	}
}

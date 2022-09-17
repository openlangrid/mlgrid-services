package org.langrid.mlgridservices.service.impl;

import java.io.File;

import org.springframework.stereotype.Service;

@Service
public class StableDiffusionTextImageGenerationService extends AbstractTextImageGenerationService{
	public StableDiffusionTextImageGenerationService(){
		super(new File("./procs/text_image_generation_stable_diffusion"));
	}
}

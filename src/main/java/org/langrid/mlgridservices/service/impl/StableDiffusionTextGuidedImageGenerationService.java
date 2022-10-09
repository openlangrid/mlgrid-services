package org.langrid.mlgridservices.service.impl;

import java.io.File;

import org.springframework.stereotype.Service;

@Service
public class StableDiffusionTextGuidedImageGenerationService extends AbstractTextGuidedImageGenerationService{
	public StableDiffusionTextGuidedImageGenerationService(){
		this("CompVis/stable-diffusion-v1-4");
	}

	public StableDiffusionTextGuidedImageGenerationService(String modelPath){
		super(new File("./procs/stable_diffusion_0_4_1"));
		setModelPath(modelPath);
	}
}

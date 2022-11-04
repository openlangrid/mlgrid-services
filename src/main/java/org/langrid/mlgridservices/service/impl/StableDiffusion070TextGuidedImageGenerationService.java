package org.langrid.mlgridservices.service.impl;

import java.io.File;

import org.springframework.stereotype.Service;

@Service
public class StableDiffusion070TextGuidedImageGenerationService extends AbstractTextGuidedImageGenerationService{
	public StableDiffusion070TextGuidedImageGenerationService(){
		this("runwayml/stable-diffusion-v1-5");
	}

	public StableDiffusion070TextGuidedImageGenerationService(String modelPath){
		super(new File("./procs/diffusers_0_7_0"));
		setModelPath(modelPath);
	}
}

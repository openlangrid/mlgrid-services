package org.langrid.mlgridservices.service.impl;

import java.io.File;

import org.springframework.stereotype.Service;

@Service
public class StableDiffusion060TextGuidedImageGenerationService extends AbstractTextGuidedImageGenerationService{
	public StableDiffusion060TextGuidedImageGenerationService(){
		this("runwayml/stable-diffusion-v1-5");
	}

	public StableDiffusion060TextGuidedImageGenerationService(String modelPath){
		super(new File("./procs/stable_diffusion_0_6_0"));
		setModelPath(modelPath);
	}
}

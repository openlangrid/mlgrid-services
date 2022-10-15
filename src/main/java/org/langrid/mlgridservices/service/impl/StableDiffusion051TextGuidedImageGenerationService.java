package org.langrid.mlgridservices.service.impl;

import java.io.File;

import org.springframework.stereotype.Service;

@Service
public class StableDiffusion051TextGuidedImageGenerationService extends AbstractTextGuidedImageGenerationService{
	public StableDiffusion051TextGuidedImageGenerationService(){
		this("CompVis/stable-diffusion-v1-4");
	}

	public StableDiffusion051TextGuidedImageGenerationService(String modelPath){
		super(new File("./procs/stable_diffusion_0_5_1"));
		setModelPath(modelPath);
	}
}

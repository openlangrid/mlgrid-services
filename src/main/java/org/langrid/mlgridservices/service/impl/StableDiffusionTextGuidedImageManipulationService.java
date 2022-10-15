package org.langrid.mlgridservices.service.impl;

import java.io.File;

import org.springframework.stereotype.Service;

@Service
public class StableDiffusionTextGuidedImageManipulationService extends AbstractTextGuidedImageManipulationService{
	public StableDiffusionTextGuidedImageManipulationService(){
		this("CompVis/stable-diffusion-v1-4");
	}

	public StableDiffusionTextGuidedImageManipulationService(String modelPath){
		super(new File("./procs/stable_diffusion_0_4_1"));
		setModelPath(modelPath);
	}
}

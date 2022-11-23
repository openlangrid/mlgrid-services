package org.langrid.mlgridservices.service.impl;

import java.io.File;

import org.springframework.stereotype.Service;

@Service
public class DiffusersTextGuidedImageGenerationService extends AbstractTextGuidedImageGenerationService{
	public DiffusersTextGuidedImageGenerationService(){
		this("./procs/diffusers_0_7_0", "runwayml/stable-diffusion-v1-5");
	}

	public DiffusersTextGuidedImageGenerationService(String procPath, String modelPath){
		super(new File(procPath));
		setModelPath(modelPath);
	}
}

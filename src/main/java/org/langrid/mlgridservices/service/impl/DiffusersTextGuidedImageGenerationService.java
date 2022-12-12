package org.langrid.mlgridservices.service.impl;

import java.io.File;

import org.springframework.stereotype.Service;

@Service
public class DiffusersTextGuidedImageGenerationService extends AbstractTextGuidedImageGenerationService{
	public DiffusersTextGuidedImageGenerationService(){
		this("./procs/diffusers_0_7_0", "runwayml/stable-diffusion-v1-5");
	}

	public DiffusersTextGuidedImageGenerationService(String procPath, String modelPath){
		this(procPath, modelPath, null);
	}

	public DiffusersTextGuidedImageGenerationService(String procPath, String modelPath,
		String additionalPrompt){
		super(new File(procPath));
		setModelPath(modelPath);
		setAdditionalPrompt(additionalPrompt);
	}

	public DiffusersTextGuidedImageGenerationService(String procPath, String modelPath,
		String additionalPrompt, String scriptFile){
		super(new File(procPath));
		setModelPath(modelPath);
		setAdditionalPrompt(additionalPrompt);
		setScriptFile(scriptFile);
	}
}

package org.langrid.mlgridservices.config;

import org.langrid.mlgridservices.service.impl.VoskContinuousSpeechRecognitionService;
import org.langrid.service.ml.ContinuousSpeechRecognitionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {
	@Bean
	public ContinuousSpeechRecognitionService voskSpeechRecognition(){
		return new VoskContinuousSpeechRecognitionService();
	}
}

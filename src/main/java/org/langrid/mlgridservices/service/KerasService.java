package org.langrid.mlgridservices.service;

import java.util.Base64;

import org.langrid.mlgridservices.controller.Request;
import org.langrid.mlgridservices.controller.Response;
import org.langrid.mlgridservices.service.impl.KerasImageClassificationService;
import org.langrid.service.ml.ImageClassificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import jp.go.nict.langrid.commons.lang.ObjectUtil;

@org.springframework.stereotype.Service
public class KerasService implements Service {
	@Override
	public Response invoke(String serviceId, Request invocation) {
		try{
			var a2 = invocation.getArgs()[1];
			if(a2 instanceof String){
				invocation.getArgs()[1] = Base64.getDecoder().decode((String)a2);
			}
			return new Response(
					ObjectUtil.invoke(service(serviceId), invocation.getMethod(), invocation.getArgs()));
		} catch(RuntimeException e){
			throw e;
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private ImageClassificationService service(String serviceId){
		switch(serviceId){
			case "YoloV5n": return kerasEfficientNetV2B0();
			case "YoloV5m": return kerasResNet50();
			default: 
			case "YoloV5s": return kerasVGG19();
		}
	}

	@Bean
	private ImageClassificationService kerasEfficientNetV2B0(){
		return new KerasImageClassificationService(dockerServiceName, "EfficientNetV2B0");
	}

	@Bean
	private ImageClassificationService kerasResNet50(){
		return new KerasImageClassificationService(dockerServiceName, "ResNet50");
	}

	@Bean
	private ImageClassificationService kerasVGG19(){
		return new KerasImageClassificationService(dockerServiceName, "VGG19");
	}

	@Value("${mlgrid-services.keras.docker-service-name}")
	private String dockerServiceName;
}

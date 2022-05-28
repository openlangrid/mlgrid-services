package org.langrid.mlgridservices.service.group;

import org.langrid.mlgridservices.controller.Request;
import org.langrid.mlgridservices.controller.Response;
import org.langrid.mlgridservices.service.impl.DalleMiniTextImageGenerationService;
import org.langrid.service.ml.TextToImageGenerationService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import jp.go.nict.langrid.commons.lang.ObjectUtil;

@Service
public class DalleMiniService  implements ServiceGroup {
	@Override
	public Response invoke(String serviceId, Request invocation) {
		try{
			return new Response(
					ObjectUtil.invoke(service(serviceId), invocation.getMethod(), invocation.getArgs()));
		} catch(RuntimeException e){
			throw e;
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private TextToImageGenerationService service(String serviceId){
		switch(serviceId){
			default: 
			case "DalleMiniMega1Fp16": return dalleMiniMega1Fp16();
			case "DalleMiniMini1": return dalleMiniMini1();
		}
	}

	@Bean
	private TextToImageGenerationService dalleMiniMega1Fp16(){
		return new DalleMiniTextImageGenerationService("dalle-mini/dalle-mini/mega-1-fp16:latest");
	}

	@Bean
	private TextToImageGenerationService dalleMiniMini1(){
		return new DalleMiniTextImageGenerationService("dalle-mini/dalle-mini/mini-1:v0");
	}
}

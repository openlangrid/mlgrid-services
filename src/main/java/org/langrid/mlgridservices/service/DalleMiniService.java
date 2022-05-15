package org.langrid.mlgridservices.service;

import org.langrid.mlgridservices.controller.Request;
import org.langrid.mlgridservices.controller.Response;
import org.langrid.mlgridservices.service.impl.DalleMiniTextImageGenerationService;
import org.langrid.service.ml.TextImageGenerationService;
import org.springframework.context.annotation.Bean;

import jp.go.nict.langrid.commons.lang.ObjectUtil;

@org.springframework.stereotype.Service
public class DalleMiniService  implements Service {
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

	private TextImageGenerationService service(String serviceId){
		switch(serviceId){
			default: 
			case "DalleMiniMega1Fp16": return dalleMiniMega1Fp16();
		}
	}

	@Bean
	private TextImageGenerationService dalleMiniMega1Fp16(){
		return new DalleMiniTextImageGenerationService();
	}
}

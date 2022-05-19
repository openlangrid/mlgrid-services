package org.langrid.mlgridservices.service;

import org.langrid.mlgridservices.controller.Request;
import org.langrid.mlgridservices.controller.Response;
import org.langrid.mlgridservices.service.impl.HelsinkiNlpTranslationService;
import org.langrid.mlgridservices.service.impl.HuggingFaceTextSentimentAnalysisService;
import org.langrid.service.ml.TextSentimentAnalysisService;
import org.springframework.context.annotation.Bean;

import jp.go.nict.langrid.commons.lang.ObjectUtil;

@org.springframework.stereotype.Service
public class HuggingFaceService  implements Service {
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

	private TextSentimentAnalysisService service(String serviceId){
		switch(serviceId){
			default: 
			case "ClTohokuSentimentAnalysis": return huggingFaceSAService();
		}
	}

	@Bean
	private TextSentimentAnalysisService huggingFaceSAService(){
		return new HuggingFaceTextSentimentAnalysisService();
	}
}

package org.langrid.mlgridservices.service.group;

import java.util.Arrays;
import java.util.List;

import org.langrid.mlgridservices.controller.Request;
import org.langrid.mlgridservices.controller.Response;
import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.service.impl.HuggingFaceTextSentimentAnalysisService;
import org.langrid.service.ml.TextSentimentAnalysisService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import jp.go.nict.langrid.commons.lang.ObjectUtil;
import jp.go.nict.langrid.commons.util.Pair;

@Service
public class HuggingFaceServiceGroup  implements ServiceGroup {
	@Override
	public List<Pair<String, Class<?>>> listServices() {
		Class<?> clazz = TextSentimentAnalysisService.class;
		return Arrays.asList(
			Pair.create("ClTohokuSentimentAnalysis", clazz)
			);
	}

	@Override
	public Response invoke(String serviceId, Request invocation) {
		try(var t = ServiceInvokerContext.startServiceTimer()){
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

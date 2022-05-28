package org.langrid.mlgridservices.service.group;

import org.langrid.mlgridservices.controller.Request;
import org.langrid.mlgridservices.controller.Response;
import org.langrid.mlgridservices.service.impl.HelsinkiNlpTranslationService;
import org.springframework.context.annotation.Bean;

import jp.go.nict.langrid.commons.lang.ObjectUtil;
import jp.go.nict.langrid.service_1_2.translation.TranslationService;

@org.springframework.stereotype.Service
public class HelsinkiNlpService  implements ServiceGroup {
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

	private TranslationService service(String serviceId){
		switch(serviceId){
			default: 
			case "HelsinkiNlpOpusMtEnJa": return helsinkiNlpOpusMtEnJa();
			case "HelsinkiNlpOpusMtJaEn": return helsinkiNlpOpusMtJaEn();
		}
	}

	@Bean
	private TranslationService helsinkiNlpOpusMtEnJa(){
		return new HelsinkiNlpTranslationService("opus-mt-en-jap");
	}

	@Bean
	private TranslationService helsinkiNlpOpusMtJaEn(){
		return new HelsinkiNlpTranslationService("opus-mt-ja-en");
	}
}

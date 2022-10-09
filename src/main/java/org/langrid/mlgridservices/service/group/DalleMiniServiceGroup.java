package org.langrid.mlgridservices.service.group;

import org.langrid.mlgridservices.controller.Request;
import org.langrid.mlgridservices.controller.Response;
import org.langrid.mlgridservices.service.impl.DalleMiniTextImageGenerationService;
import org.langrid.service.ml.interim.TextGuidedImageGenerationService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import jp.go.nict.langrid.commons.beanutils.Converter;
import jp.go.nict.langrid.commons.lang.ClassUtil;
import jp.go.nict.langrid.commons.lang.ObjectUtil;

@Service
public class DalleMiniServiceGroup  implements ServiceGroup {
	@Override
	public Response invoke(String serviceId, Request invocation) {
		try{
			var s = service(serviceId);
			var mn = invocation.getMethod();
			var m = ClassUtil.findMethod(s.getClass(), mn, invocation.getArgs().length);
			var args = new Converter().convertEachElement(invocation.getArgs(), m.getParameterTypes());
			return new Response(ObjectUtil.invoke(s, mn, args));
		} catch(RuntimeException e){
			throw e;
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private TextGuidedImageGenerationService service(String serviceId){
		switch(serviceId){
			default: 
			case "DalleMiniMega1Fp16": return dalleMiniMega1Fp16();
			case "DalleMiniMini1": return dalleMiniMini1();
		}
	}

	@Bean
	private TextGuidedImageGenerationService dalleMiniMega1Fp16(){
		return new DalleMiniTextImageGenerationService("dalle-mini/dalle-mini/mega-1-fp16:latest");
	}

	@Bean
	private TextGuidedImageGenerationService dalleMiniMini1(){
		return new DalleMiniTextImageGenerationService("dalle-mini/dalle-mini/mini-1:v0");
	}
}

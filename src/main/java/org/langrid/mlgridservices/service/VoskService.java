package org.langrid.mlgridservices.service;

import org.langrid.mlgridservices.controller.Request;
import org.langrid.mlgridservices.controller.Response;
import org.langrid.service.ml.ContinuousSpeechRecognitionService;
import org.springframework.beans.factory.annotation.Autowired;

import jp.go.nict.langrid.commons.beanutils.Converter;
import jp.go.nict.langrid.commons.lang.ClassUtil;
import jp.go.nict.langrid.commons.lang.ObjectUtil;

@org.springframework.stereotype.Service
public class VoskService implements Service {
    @Override
	public Response invoke(String serviceId, Request invocation) {
		try{
			var s = service(serviceId);
			var mn = invocation.getMethod();
			var m = ClassUtil.findMethod(s.getClass(), mn, invocation.getArgs().length);
			Converter c = new Converter();
//			c.addTransformerConversion(String.class, byte[].class, v->Base64.getDecoder().decode(v));
			var args = c.convertEachElement(invocation.getArgs(), m.getParameterTypes());
			return new Response(ObjectUtil.invoke(service(serviceId), mn, args));
		} catch(RuntimeException e){
			throw e;
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private ContinuousSpeechRecognitionService service(String serviceId){
		switch(serviceId){
			default: 
			case "VOSK": return voskSpeechRecognition;
		}
	}

	@Autowired
	private ContinuousSpeechRecognitionService voskSpeechRecognition;
}

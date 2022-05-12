package org.langrid.jsservicegw.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.langrid.jsservicegw.controller.Request;
import org.langrid.jsservicegw.controller.Response;
import org.springframework.beans.factory.annotation.Value;

import jp.go.nict.langrid.client.soap.SoapClientFactory;
import jp.go.nict.langrid.commons.lang.ObjectUtil;
import jp.go.nict.langrid.service_1_2.translation.TranslationService;

@org.springframework.stereotype.Service
public class LangridService implements Service{
	public Response invoke(String serviceId, Request invocation) {
		try{
			var c = newClient(serviceId, intfs.get(serviceId));
			var r = ObjectUtil.invoke(c, invocation.getMethod(), invocation.getArgs());
			return new Response(r);
		} catch(RuntimeException e){
			throw e;
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private Object newClient(String serviceId, Class<?> intfClass)
			throws MalformedURLException{
		return new SoapClientFactory().create(
			intfClass, new URL(url + serviceId), username, password);
	}

	private static Map<String, Class<?>> intfs = new HashMap<>();
	static{
		intfs.put("GoogleTranslateNMT", TranslationService.class);
	}

	@Value("${jsservicegw.langrid.url}")
	private String url;
	@Value("${jsservicegw.langrid.username}")
	private String username;
	@Value("${jsservicegw.langrid.password}")
	private String password;
}

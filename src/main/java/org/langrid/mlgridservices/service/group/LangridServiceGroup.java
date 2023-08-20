package org.langrid.mlgridservices.service.group;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.langrid.service.ml.TranslationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jp.go.nict.langrid.client.soap.SoapClientFactory;
import jp.go.nict.langrid.commons.util.Pair;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.LangridException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguagePairException;

@Service
public class LangridServiceGroup implements ServiceGroup{
	@Override
	public List<Pair<String, Class<?>>> listServices() {
		Class<?> clazz = TranslationService.class;
		return Arrays.asList(
				Pair.create("LangridGoogleTranslateNMT", clazz)
//				Pair.create("LangridKyotoUJServer", clazz)
			);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(String serviceId) {
		if(serviceId.startsWith("Langrid")){
			serviceId = serviceId.substring("Langrid".length());
		}
		try{
			return (T)newClient(serviceId, intfs.get(serviceId));
		} catch(RuntimeException e){
			throw e;
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T newClient(String serviceId, Class<T> intfClass)
			throws MalformedURLException{
		if(intfClass.equals(TranslationService.class)){
			var orig = new SoapClientFactory().create(
				jp.go.nict.langrid.service_1_2.translation.TranslationService.class,
				new URL(url + serviceId), username, password);
			return (T)new TranslationService(){
				@Override
				public String translate(String text, String textLanguage, String targetLanguage)
						throws InvalidParameterException, ProcessFailedException, UnsupportedLanguagePairException {
					try{
						return orig.translate(textLanguage, targetLanguage, text);
					} catch(InvalidParameterException | ProcessFailedException e){
						throw e;
					} catch(LangridException e){
						throw new ProcessFailedException(e);
					}
				}
			};
		}
		return new SoapClientFactory().create(
			intfClass, new URL(url + serviceId), username, password);
	}

	private static Map<String, Class<?>> intfs = new HashMap<>();
	static{
		intfs.put("GoogleTranslateNMT", TranslationService.class);
		intfs.put("KyotoUJServer", TranslationService.class);
	}

	@Value("${services.langrid.url}")
	private String url;
	@Value("${services.langrid.username}")
	private String username;
	@Value("${services.langrid.password}")
	private String password;
}

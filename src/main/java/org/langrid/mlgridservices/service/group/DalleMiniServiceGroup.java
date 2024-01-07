package org.langrid.mlgridservices.service.group;

import java.util.Arrays;
import java.util.List;

import org.langrid.mlgridservices.service.impl.DalleMiniTextImageGenerationService;
import org.langrid.service.ml.Image;
import org.langrid.service.ml.TextGuidedImageGenerationService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import jp.go.nict.langrid.commons.util.Pair;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

@Service
public class DalleMiniServiceGroup  implements ServiceGroup {
	@Override
	public List<Pair<String, Class<?>>> listServices() {
		Class<?> clazz = TextGuidedImageGenerationService.class;
		return Arrays.asList(
			Pair.create("DalleMiniMega1Fp16", clazz),
			Pair.create("DalleMiniMini1", clazz)
			);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String serviceId) {
		try{
			var s = service(serviceId);
			return (T)new TextGuidedImageGenerationService(){
				@Override
				public Image[] generateMultiTimes(String text, String textLanguage, int numberOfTimes)
						throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException {
					return s.generateMultiTimes(text, textLanguage, numberOfTimes);
				}
			};
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

package org.langrid.mlgridservices.service.group;

import java.util.Arrays;
import java.util.List;

import org.langrid.mlgridservices.service.impl.KerasImageClassificationService;
import org.langrid.service.ml.ImageClassificationResult;
import org.langrid.service.ml.ImageClassificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import jp.go.nict.langrid.commons.util.Pair;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

@Service
public class KerasServiceGroup implements ServiceGroup {
	@Override
	public List<Pair<String, Class<?>>> listServices() {
		Class<?> clazz = ImageClassificationService.class;
		return Arrays.asList(
				Pair.create("KerasEfficientNetV2B0", clazz),
				Pair.create("KerasResNet50", clazz),
				Pair.create("KerasVGG19", clazz)
			);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String serviceId) {
		var s = service(serviceId);
		return (T)new ImageClassificationService() {
			@Override
			public ImageClassificationResult[] classify(byte[] image, String format, String labelLanguage,
					int maxResults)
					throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException {
				try{
					return s.classify(image, format, labelLanguage, maxResults);
				} catch(RuntimeException e){
					throw e;
				} catch(Exception e){
					throw new RuntimeException(e);
				}
			}
		};
	}

	private ImageClassificationService service(String serviceId){
		switch(serviceId){
			case "KerasEfficientNetV2B0": return kerasEfficientNetV2B0();
			case "KerasResNet50": return kerasResNet50();
			default: 
			case "KerasVGG19": return kerasVGG19();
		}
	}

	@Bean
	private ImageClassificationService kerasEfficientNetV2B0(){
		return new KerasImageClassificationService(dockerServiceName, "EfficientNetV2B0");
	}

	@Bean
	private ImageClassificationService kerasResNet50(){
		return new KerasImageClassificationService(dockerServiceName, "ResNet50");
	}

	@Bean
	private ImageClassificationService kerasVGG19(){
		return new KerasImageClassificationService(dockerServiceName, "VGG19");
	}

	@Value("${services.keras.docker-service-name}")
	private String dockerServiceName;
}

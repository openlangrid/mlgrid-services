package org.langrid.mlgridservices.service.group;

import java.util.Arrays;
import java.util.List;

import org.langrid.mlgridservices.service.impl.YoloV5ObjectDetectionService;
import org.langrid.service.ml.ObjectDetectionResult;
import org.langrid.service.ml.ObjectDetectionService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import jp.go.nict.langrid.commons.util.Pair;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

@Service
public class YoloV5ServiceGroup implements ServiceGroup{
	@Override
	public List<Pair<String, Class<?>>> listServices() {
		Class<?> clazz = ObjectDetectionService.class;
		return Arrays.asList(
				Pair.create("YoloV5n", clazz),
				Pair.create("YoloV5s", clazz),
				Pair.create("YoloV5m", clazz),
				Pair.create("YoloV5l", clazz),
				Pair.create("YoloV5x", clazz)
			);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String serviceId) {
		var s = service(serviceId);
		return (T)new ObjectDetectionService() {
			@Override
			public ObjectDetectionResult detect(byte[] image, String imageFormat, String labelLanguage)
					throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException {
				try{
					return s.detect(image, imageFormat, labelLanguage);
				} catch(RuntimeException e){
					throw e;
				} catch(Exception e){
					throw new ProcessFailedException(e);
				}
			}
		};
	}

	private ObjectDetectionService service(String serviceId){
		switch(serviceId){
			case "YoloV5n": return yolov5n();
			default: 
			case "YoloV5s": return yolov5s();
			case "YoloV5m": return yolov5m();
			case "YoloV5l": return yolov5l();
			case "YoloV5x": return yolov5x();
		}
	}

	@Bean
	private ObjectDetectionService yolov5n(){
		return new YoloV5ObjectDetectionService("yolov5n");
	}
	@Bean
	private ObjectDetectionService yolov5s(){
		return new YoloV5ObjectDetectionService("yolov5s");
	}
	@Bean
	private ObjectDetectionService yolov5m(){
		return new YoloV5ObjectDetectionService("yolov5m");
	}
	@Bean
	private ObjectDetectionService yolov5l(){
		return new YoloV5ObjectDetectionService("yolov5l");
	}
	@Bean
	private ObjectDetectionService yolov5x(){
		return new YoloV5ObjectDetectionService("yolov5x");
	}
}

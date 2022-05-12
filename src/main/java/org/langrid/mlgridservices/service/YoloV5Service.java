package org.langrid.mlgridservices.service;

import java.util.Base64;

import org.langrid.mlgridservices.controller.Request;
import org.langrid.mlgridservices.controller.Response;
import org.langrid.mlgridservices.service.impl.YoloV5ObjectDetectionService;
import org.langrid.service.ml.ObjectDetectionService;
import org.springframework.context.annotation.Bean;

import jp.go.nict.langrid.commons.lang.ObjectUtil;

@org.springframework.stereotype.Service
public class YoloV5Service implements Service{
	@Override
	public Response invoke(String serviceId, Request invocation) {
		try{
			invocation.getArgs()[1] = Base64.getDecoder().decode((String)invocation.getArgs()[1]);
			return new Response(
					ObjectUtil.invoke(service(serviceId), invocation.getMethod(), invocation.getArgs()));
		} catch(RuntimeException e){
			throw e;
		} catch(Exception e){
			throw new RuntimeException(e);
		}
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

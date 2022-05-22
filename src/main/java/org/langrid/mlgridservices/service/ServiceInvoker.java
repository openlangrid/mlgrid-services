package org.langrid.mlgridservices.service;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.langrid.mlgridservices.controller.Request;
import org.langrid.mlgridservices.controller.Response;
import org.langrid.mlgridservices.service.impl.DummyTextImageGenerationService;
import org.langrid.service.ml.TextImageGenerationService;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Service
public class ServiceInvoker {
	@PostConstruct
	private synchronized void init() {
		services.put("ClTohokuSentimentAnalysis", huggingFaceService);
		services.put("DalleMiniMega1Fp16", dalleMiniService);
		services.put("DalleMiniMini1", dalleMiniService);
		services.put("DummyTextImageGeneration", new Service(){
			private TextImageGenerationService s = new DummyTextImageGenerationService();
			@Override
			public Response invoke(String serviceId, Request invocation) {
				try{
					return new Response(s.generate("", "", "", 1));
				} catch(Exception e){
					throw new RuntimeException(e);
				}
			}
		});
		services.put("HelsinkiNLPOpusMT", helsinkiNlpService);
		services.put("KerasResNet50", kerasService);
		services.put("KerasEfficientNetV2B0", kerasService);
		services.put("KerasVGG19", kerasService);
		services.put("LangridGoogleTranslateNMT", langridService);
		services.put("LangridKyotoUJServer", langridService);
		services.put("YoloV5n", yoloV5Service);
		services.put("YoloV5s", yoloV5Service);
		services.put("YoloV5m", yoloV5Service);
		services.put("YoloV5l", yoloV5Service);
		services.put("YoloV5x", yoloV5Service);
		services.put("VOSK", voskService);
	}

	public synchronized Response invoke(
			String serviceId,
			Request invocation) throws MalformedURLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		var service = services.get(serviceId);
		System.out.printf("[invoke] %s -> %s%n", serviceId, service);
		if(service != null) {
			return service.invoke(serviceId, invocation);
		} else {
			return langridService.invoke(serviceId, invocation);
		}
	}

	private Map<String, Service> services = new HashMap<>();

	@Autowired
	private LangridService langridService;
	@Autowired
	private HuggingFaceService huggingFaceService;
	@Autowired
	private HelsinkiNlpService helsinkiNlpService;
	@Autowired
	private KerasService kerasService;
	@Autowired
	private YoloV5Service yoloV5Service;
	@Autowired
	private DalleMiniService dalleMiniService;
	@Autowired
	private VoskService voskService;
}

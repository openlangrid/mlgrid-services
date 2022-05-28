package org.langrid.mlgridservices.service;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.langrid.mlgridservices.controller.Request;
import org.langrid.mlgridservices.controller.Response;
import org.langrid.mlgridservices.service.group.DalleMiniService;
import org.langrid.mlgridservices.service.group.HelsinkiNlpService;
import org.langrid.mlgridservices.service.group.HuggingFaceService;
import org.langrid.mlgridservices.service.group.KerasService;
import org.langrid.mlgridservices.service.group.LangridService;
import org.langrid.mlgridservices.service.group.ServiceGroup;
import org.langrid.mlgridservices.service.group.YoloV5Service;
import org.langrid.mlgridservices.service.impl.DummyTextImageGenerationService;
import org.langrid.mlgridservices.service.impl.VoskSpeechRecognitionService;
import org.langrid.service.ml.TextToImageGenerationService;
import org.springframework.beans.factory.annotation.Autowired;

import jp.go.nict.langrid.commons.beanutils.Converter;
import jp.go.nict.langrid.commons.lang.ClassUtil;
import jp.go.nict.langrid.commons.lang.ObjectUtil;

@org.springframework.stereotype.Service
public class ServiceInvoker {
	@PostConstruct
	private synchronized void init() {
		serviceGroups.put("ClTohokuSentimentAnalysis", huggingFaceService);
		serviceGroups.put("DalleMiniMega1Fp16", dalleMiniService);
		serviceGroups.put("DalleMiniMini1", dalleMiniService);
		serviceGroups.put("DummyTextImageGeneration", new ServiceGroup(){
			private TextToImageGenerationService s = new DummyTextImageGenerationService();
			@Override
			public Response invoke(String serviceId, Request invocation) {
				try{
					return new Response(s.generate("", "", "", 1));
				} catch(Exception e){
					throw new RuntimeException(e);
				}
			}
		});
		serviceGroups.put("HelsinkiNLPOpusMT", helsinkiNlpService);
		serviceGroups.put("KerasResNet50", kerasService);
		serviceGroups.put("KerasEfficientNetV2B0", kerasService);
		serviceGroups.put("KerasVGG19", kerasService);
		serviceGroups.put("LangridGoogleTranslateNMT", langridService);
		serviceGroups.put("LangridKyotoUJServer", langridService);
		serviceGroups.put("YoloV5n", yoloV5Service);
		serviceGroups.put("YoloV5s", yoloV5Service);
		serviceGroups.put("YoloV5m", yoloV5Service);
		serviceGroups.put("YoloV5l", yoloV5Service);
		serviceGroups.put("YoloV5x", yoloV5Service);
		serviceImples.put("VOSK", vosk);
	}

	public synchronized Response invoke(String serviceId, Request invocation)
	throws MalformedURLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		var s = serviceImples.get(serviceId);
		if(s != null){
			System.out.printf("[invokeService] %s -> %s%n", serviceId, s);
			var mn = invocation.getMethod();
			var m = ClassUtil.findMethod(s.getClass(), mn, invocation.getArgs().length);
			var args = c.convertEachElement(invocation.getArgs(), m.getParameterTypes());
			return new Response(ObjectUtil.invoke(s, mn, args));
		}
		var g = serviceGroups.get(serviceId);
		System.out.printf("[invokeGroup] %s -> %s%n", serviceId, g);
		if(g != null) {
			return g.invoke(serviceId, invocation);
		} else {
			return langridService.invoke(serviceId, invocation);
		}
	}

	private Converter c = new Converter();
	private Map<String, ServiceGroup> serviceGroups = new HashMap<>();
	private Map<String, Object> serviceImples = new HashMap<>();

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
	private VoskSpeechRecognitionService vosk;
}

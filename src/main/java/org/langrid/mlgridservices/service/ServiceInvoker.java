package org.langrid.mlgridservices.service;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.langrid.mlgridservices.controller.Request;
import org.langrid.mlgridservices.controller.Response;
import org.langrid.mlgridservices.service.group.DalleMiniServiceGroup;
import org.langrid.mlgridservices.service.group.HuggingFaceServiceGroup;
import org.langrid.mlgridservices.service.group.KerasServiceGroup;
import org.langrid.mlgridservices.service.group.LangridServiceGroup;
import org.langrid.mlgridservices.service.group.ServiceGroup;
import org.langrid.mlgridservices.service.group.YoloV5ServiceGroup;
import org.langrid.mlgridservices.service.impl.DummyTextImageGenerationService;
import org.langrid.mlgridservices.service.impl.EmpathService;
import org.langrid.mlgridservices.service.impl.GoogleTextToSpeechService;
import org.langrid.mlgridservices.service.impl.HelsinkiNlpTranslationService;
import org.langrid.mlgridservices.service.impl.OpenPoseHumanPoseEstimationService;
import org.langrid.mlgridservices.service.impl.VoskSpeechRecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.go.nict.langrid.commons.beanutils.Converter;
import jp.go.nict.langrid.commons.lang.ClassUtil;
import jp.go.nict.langrid.commons.lang.ObjectUtil;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;

@Service
public class ServiceInvoker {
	@PostConstruct
	private void init() {
		// serviceImplesにはあるサービスIDに対応する実装クラスを登録する。
		serviceImples.put("VOSK", new VoskSpeechRecognitionService());
		serviceImples.put("HelsinkiNLPOpusMT", new HelsinkiNlpTranslationService());
		serviceImples.put("DummyTextImageGeneration", new DummyTextImageGenerationService());
		serviceImples.put("Empath", empathService);
		serviceImples.put("GoogleTTS", googleTtsService);
		serviceImples.put("OpenPose", new OpenPoseHumanPoseEstimationService());
		// serviceGroupsは共通のprefixを持つサービス群をまとめたサービスグループを登録する。
		serviceGroups.put("ClTohokuSentimentAnalysis", huggingFaceServices);
		serviceGroups.put("DalleMini", dalleMiniServices);
		serviceGroups.put("Keras", kerasServices);
		serviceGroups.put("Langrid", langridServices);
		serviceGroups.put("YoloV5", yoloV5Services);
	}

	public Response invoke(String serviceId, Request invocation)
	throws MalformedURLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
	ProcessFailedException{
		// serviceIdに対応する実装クラスを探す
		var s = serviceImples.get(serviceId);
		if(s != null){
			System.out.printf("[invokeService] %s -> %s%n", serviceId, s);
			var mn = invocation.getMethod();
			var m = ClassUtil.findMethod(s.getClass(), mn, invocation.getArgs().length);
			var args = c.convertEachElement(invocation.getArgs(), m.getParameterTypes());
			return new Response(ObjectUtil.invoke(s, mn, args));
		}
		// 実装クラスがなければグループを探す
		var g = serviceGroups.get(serviceId);
		if(g == null) {
			// 見つからなければ前方一致で検索
			for(var e : serviceGroups.entrySet()) {
				if(!serviceId.startsWith(e.getKey())) continue;
				g = e.getValue();
			}
		}
		if(g == null) {
			throw new ProcessFailedException("service " + serviceId + " not found.");
		}
		System.out.printf("[invokeGroup] %s -> %s%n", serviceId, g);
		var r = g.invoke(serviceId, invocation);
		serviceGroups.put(serviceId, g); // 処理が正常に終了した場合はserviceIdとグループを関連付けておく。
		return r;
	}

	private Converter c = new Converter();
	private Map<String, ServiceGroup> serviceGroups = new HashMap<>();
	private Map<String, Object> serviceImples = new HashMap<>();

	@Autowired
	private EmpathService empathService;
	@Autowired
	private GoogleTextToSpeechService googleTtsService;

	@Autowired
	private LangridServiceGroup langridServices;
	@Autowired
	private HuggingFaceServiceGroup huggingFaceServices;
	@Autowired
	private KerasServiceGroup kerasServices;
	@Autowired
	private YoloV5ServiceGroup yoloV5Services;
	@Autowired
	private DalleMiniServiceGroup dalleMiniServices;
}

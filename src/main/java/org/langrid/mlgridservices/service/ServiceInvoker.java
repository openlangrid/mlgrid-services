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
import org.langrid.mlgridservices.service.impl.ClipInterrogatorImageToTextService;
import org.langrid.mlgridservices.service.impl.CodeFormerImageToImageConversionService;
import org.langrid.mlgridservices.service.impl.DiffusersTextGuidedImageGenerationService;
import org.langrid.mlgridservices.service.impl.DummySpeechRecognitionService;
import org.langrid.mlgridservices.service.impl.DummyTextImageGenerationService;
import org.langrid.mlgridservices.service.impl.EmpathService;
import org.langrid.mlgridservices.service.impl.GoogleTextToSpeechService;
import org.langrid.mlgridservices.service.impl.HelsinkiNlpTranslationService;
import org.langrid.mlgridservices.service.impl.OpenPoseHumanPoseEstimationService;
import org.langrid.mlgridservices.service.impl.RealEsrganImageToImageConversionService;
import org.langrid.mlgridservices.service.impl.RinnaJapaneseStableDiffusionTextImageGenerationService;
import org.langrid.mlgridservices.service.impl.SpeechBrainSpeechEmotionRecognitionService;
import org.langrid.mlgridservices.service.impl.StableDiffusion051TextGuidedImageGenerationService;
import org.langrid.mlgridservices.service.impl.StableDiffusion060TextGuidedImageGenerationService;
import org.langrid.mlgridservices.service.impl.StableDiffusion070TextGuidedImageGenerationService;
import org.langrid.mlgridservices.service.impl.StableDiffusionTextGuidedImageManipulationService;
import org.langrid.mlgridservices.service.impl.StableDiffusionTextGuidedImageGenerationService;
import org.langrid.mlgridservices.service.impl.VoskContinuousSpeechRecognitionService;
import org.langrid.mlgridservices.service.impl.WaifuDiffusionTextImageGenerationService;
import org.langrid.mlgridservices.service.impl.WhisperSpeechRecognitionService;
import org.langrid.mlgridservices.service.management.ServiceManagement;
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
		serviceImples.put("DummySpeechRecognition", new DummySpeechRecognitionService());
		serviceImples.put("DummyTextImageGeneration", new DummyTextImageGenerationService());
		serviceImples.put("CodeFormer", codeFormer);
		serviceImples.put("Empath", empath);
		serviceImples.put("GoogleTTS", googleTts);
		serviceImples.put("HelsinkiNLPOpusMT", new HelsinkiNlpTranslationService());
		serviceImples.put("ClipInterrogator", new ClipInterrogatorImageToTextService());
		serviceImples.put("OpenPose", new OpenPoseHumanPoseEstimationService());
		serviceImples.put("RealESRGAN", new RealEsrganImageToImageConversionService());
		serviceImples.put("RinnaJapaneseStableDiffusion", rinnaJapaneseStableDiffusion);
		serviceImples.put("SpeechBrainSER", new SpeechBrainSpeechEmotionRecognitionService());
		serviceImples.put("VOSK", new VoskContinuousSpeechRecognitionService());
		serviceImples.put("Whisper", new WhisperSpeechRecognitionService());


		serviceImples.put("WaifuDiffusionDS030", waifuDiffusion);
		serviceImples.put("StableDiffusionDS041", stableDiffusion);
		serviceImples.put("DiscoDiffusionDS041", new StableDiffusionTextGuidedImageGenerationService("sd-dreambooth-library/disco-diffusion-style"));
		serviceImples.put("WaifuDiffusionDS041", new StableDiffusionTextGuidedImageGenerationService("hakurei/waifu-diffusion"));
		serviceImples.put("TrinartStableDiffusionDS041", new StableDiffusionTextGuidedImageGenerationService("naclbit/trinart_stable_diffusion_v2"));
		serviceImples.put("TrinartWaifuDS041", new StableDiffusionTextGuidedImageGenerationService("doohickey/trinart-waifu-diffusion-50-50"));
		serviceImples.put("StableDiffusionDS051", new StableDiffusion051TextGuidedImageGenerationService());
		serviceImples.put("DiscoDiffusionDS051", new StableDiffusion051TextGuidedImageGenerationService("sd-dreambooth-library/disco-diffusion-style"));
		serviceImples.put("WaifuDiffusionDS051", new StableDiffusion051TextGuidedImageGenerationService("hakurei/waifu-diffusion"));
		serviceImples.put("TrinartStableDiffusionDS051", new StableDiffusion051TextGuidedImageGenerationService("naclbit/trinart_stable_diffusion_v2"));
		serviceImples.put("TrinartWaifuDS051", new StableDiffusion051TextGuidedImageGenerationService("doohickey/trinart-waifu-diffusion-50-50"));
		serviceImples.put("StableDiffusionDS060SD15", new StableDiffusion060TextGuidedImageGenerationService());
		serviceImples.put("StableDiffusionDS060SD14", new StableDiffusion060TextGuidedImageGenerationService());
		serviceImples.put("DiscoDiffusionDS060", new StableDiffusion060TextGuidedImageGenerationService("sd-dreambooth-library/disco-diffusion-style"));
		serviceImples.put("WaifuDiffusionDS060", new StableDiffusion060TextGuidedImageGenerationService("hakurei/waifu-diffusion"));
		serviceImples.put("TrinartStableDiffusionDS060", new StableDiffusion060TextGuidedImageGenerationService("naclbit/trinart_stable_diffusion_v2"));
		serviceImples.put("TrinartWaifuDS060", new StableDiffusion060TextGuidedImageGenerationService("doohickey/trinart-waifu-diffusion-50-50"));
		serviceImples.put("StableDiffusionDS070SD15", new StableDiffusion070TextGuidedImageGenerationService());
		serviceImples.put("StableDiffusionDS070SD14", new StableDiffusion070TextGuidedImageGenerationService());
		serviceImples.put("DiscoDiffusionDS070", new StableDiffusion070TextGuidedImageGenerationService("sd-dreambooth-library/disco-diffusion-style"));
		serviceImples.put("WaifuDiffusionDS070", new StableDiffusion070TextGuidedImageGenerationService("hakurei/waifu-diffusion"));
		serviceImples.put("TrinartStableDiffusionDS070", new StableDiffusion070TextGuidedImageGenerationService("naclbit/trinart_stable_diffusion_v2"));
		serviceImples.put("TrinartWaifuDS070", new StableDiffusion070TextGuidedImageGenerationService("doohickey/trinart-waifu-diffusion-50-50"));
		serviceImples.put("MidjourneyV4DS072", new DiffusersTextGuidedImageGenerationService(
			"./procs/diffusers_0_7_2", "prompthero/midjourney-v4-diffusion", "mdjrny-v4 style"));
		serviceImples.put("GhibliDS072", new DiffusersTextGuidedImageGenerationService(
			"./procs/diffusers_0_7_2", "nitrosocke/Ghibli-Diffusion", "ghibli style"));

		var ds081 = "./procs/diffusers_0_8_1";
		addDiffusersTGIG(ds081, "StableDiffusionDS081SD14", "CompVis/stable-diffusion-v1-4");
		addDiffusersTGIG(ds081, "StableDiffusionDS081SD15", "runwayml/stable-diffusion-v1-5");
		addDiffusersTGIG(ds081, "DiscoDiffusionDS081", "sd-dreambooth-library/disco-diffusion-style");
		addDiffusersTGIG(ds081, "WaifuDiffusionDS081", "hakurei/waifu-diffusion");
		addDiffusersTGIG(ds081, "TrinartStableDiffusionDS081", "naclbit/trinart_stable_diffusion_v2");
		addDiffusersTGIG(ds081, "ChiyodaMomoTrinartWaifuDS081", "V3B4/chiyoda-momo-trinart-waifu-diffusion-50-50");
		addDiffusersTGIG(ds081, "MidjourneyV4DS081", "prompthero/midjourney-v4-diffusion",
			"mdjrny-v4 style");
		addDiffusersTGIG(ds081, "GhibliDS081", "nitrosocke/Ghibli-Diffusion",
			"ghibli style");

		serviceImples.put("StableDiffusionV2", new DiffusersTextGuidedImageGenerationService(
			"./procs/stable_diffusion_v2", "stabilityai/stable-diffusion-2"));

		var ds090 = "./procs/diffusers_0_9_0";
		addDiffusersTGIG(ds090, "StableDiffusionDS090SD14", "CompVis/stable-diffusion-v1-4");
		addDiffusersTGIG(ds090, "StableDiffusionDS090SD15", "runwayml/stable-diffusion-v1-5");
		addDiffusersTGIG(ds090, "DiscoDiffusionDS090", "sd-dreambooth-library/disco-diffusion-style");
		addDiffusersTGIG(ds090, "WaifuDiffusionDS090", "hakurei/waifu-diffusion");
		addDiffusersTGIG(ds090, "TrinartStableDiffusionDS090", "naclbit/trinart_stable_diffusion_v2");
		addDiffusersTGIG(ds090, "ChiyodaMomoTrinartWaifuDS090", "V3B4/chiyoda-momo-trinart-waifu-diffusion-50-50");
		addDiffusersTGIG(ds090, "MidjourneyV4DS090", "prompthero/midjourney-v4-diffusion",
			"mdjrny-v4 style");
		addDiffusersTGIG(ds090, "GhibliDS090", "nitrosocke/Ghibli-Diffusion",
			"ghibli style");

		serviceImples.put("StableDiffusionIMSD041", stableDiffusionI2I);
		serviceImples.put("WaifuDiffusionIMSD041", new StableDiffusionTextGuidedImageManipulationService("hakurei/waifu-diffusion"));
		serviceImples.put("TrinartStableDiffusionIMSD041", new StableDiffusionTextGuidedImageManipulationService("naclbit/trinart_stable_diffusion_v2"));
		serviceImples.put("TrinartWaifuIMSD041", new StableDiffusionTextGuidedImageManipulationService("doohickey/trinart-waifu-diffusion-50-50"));
		serviceImples.put("DiscoDiffusionIMSD041", new StableDiffusionTextGuidedImageManipulationService("sd-dreambooth-library/disco-diffusion-style"));

		serviceImples.put("ServiceManagement", new ServiceManagement(serviceGroups, serviceImples));

		// serviceGroupsは共通のprefixを持つサービス群をまとめたサービスグループを登録する。
		serviceGroups.put("ClTohokuSentimentAnalysis", huggingFaceServices);
		serviceGroups.put("DalleMini", dalleMiniServices);
		serviceGroups.put("Keras", kerasServices);
		serviceGroups.put("Langrid", langridServices);
		serviceGroups.put("YoloV5", yoloV5Services);

	}
	
	private void addDiffusersTGIG(String procPath, String name, String modelPath){
		serviceImples.put(name, new DiffusersTextGuidedImageGenerationService(
			procPath, modelPath));
	}

	private void addDiffusersTGIG(String procPath, String name, String modelPath, String additionalPrompt){
		serviceImples.put(name, new DiffusersTextGuidedImageGenerationService(
			procPath, modelPath, additionalPrompt));
	}

	public Response invoke(String serviceId, Request invocation)
	throws MalformedURLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
	ProcessFailedException{
		try(var sic = ServiceInvokerContext.create()){
			// serviceIdに対応する実装クラスを探す
			var s = serviceImples.get(serviceId);
			if(s != null){
				System.out.printf("[invokeService] %s -> %s%n", serviceId, s);
				var mn = invocation.getMethod();
				var m = ClassUtil.findMethod(s.getClass(), mn, invocation.getArgs().length);
				if(m == null){
					throw new NoSuchMethodException(String.format("Failed to find %s.%s(%d args)", serviceId, mn, invocation.getArgs().length));
				}
				var args = c.convertEachElement(invocation.getArgs(), m.getParameterTypes());
				var r = new Response(ObjectUtil.invoke(s, mn, args));
				sic.timer().close();
				r.putHeader("timer", sic.timer());
				return r;
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
			sic.timer().close();
			r.putHeader("timer", sic.timer());
			serviceGroups.put(serviceId, g); // 処理が正常に終了した場合はserviceIdとグループを関連付けておく。
			return r;
		}
	}

	private Converter c = new Converter();
	private Map<String, ServiceGroup> serviceGroups = new HashMap<>();
	private Map<String, Object> serviceImples = new HashMap<>();

	@Autowired
	private EmpathService empath;
	@Autowired
	private GoogleTextToSpeechService googleTts;
	@Autowired
	private StableDiffusionTextGuidedImageGenerationService stableDiffusion;
	@Autowired
	private RinnaJapaneseStableDiffusionTextImageGenerationService rinnaJapaneseStableDiffusion;
	@Autowired
	private WaifuDiffusionTextImageGenerationService waifuDiffusion;
	@Autowired
	private CodeFormerImageToImageConversionService codeFormer;
	@Autowired
	private StableDiffusionTextGuidedImageManipulationService stableDiffusionI2I;

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

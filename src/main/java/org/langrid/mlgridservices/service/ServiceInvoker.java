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
import org.langrid.mlgridservices.service.impl.YoloV7ObjectDetectionService;
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


		serviceImples.put("WaifuDiffusionDS0_03_0", waifuDiffusion);
		{
			var postfix = "DS_0_04_1";
			serviceImples.put("StableDiffusion" + postfix, stableDiffusion);
			serviceImples.put("WaifuDiffusion" + postfix, new StableDiffusionTextGuidedImageGenerationService("hakurei/waifu-diffusion"));
			serviceImples.put("TrinartStableDiffusion" + postfix, new StableDiffusionTextGuidedImageGenerationService("naclbit/trinart_stable_diffusion_v2"));
			serviceImples.put("DiscoDiffusion" + postfix, new StableDiffusionTextGuidedImageGenerationService("sd-dreambooth-library/disco-diffusion-style"));
			serviceImples.put("TrinartWaifu" + postfix, new StableDiffusionTextGuidedImageGenerationService("doohickey/trinart-waifu-diffusion-50-50"));
		}
		{
			var postfix = "DS_0_05_1";
			serviceImples.put("StableDiffusion" + postfix, new StableDiffusion051TextGuidedImageGenerationService());
			serviceImples.put("DiscoDiffusion" + postfix, new StableDiffusion051TextGuidedImageGenerationService("sd-dreambooth-library/disco-diffusion-style"));
			serviceImples.put("WaifuDiffusion" + postfix, new StableDiffusion051TextGuidedImageGenerationService("hakurei/waifu-diffusion"));
			serviceImples.put("TrinartStableDiffusion" + postfix, new StableDiffusion051TextGuidedImageGenerationService("naclbit/trinart_stable_diffusion_v2"));
			serviceImples.put("TrinartWaifu" + postfix, new StableDiffusion051TextGuidedImageGenerationService("doohickey/trinart-waifu-diffusion-50-50"));
		}
		{
			var postfix = "DS_0_06_0";
			serviceImples.put("StableDiffusion" + postfix + "SD14", new StableDiffusion060TextGuidedImageGenerationService());
			serviceImples.put("StableDiffusion" + postfix + "SD15", new StableDiffusion060TextGuidedImageGenerationService());
			serviceImples.put("DiscoDiffusion" + postfix, new StableDiffusion060TextGuidedImageGenerationService("sd-dreambooth-library/disco-diffusion-style"));
			serviceImples.put("WaifuDiffusion" + postfix, new StableDiffusion060TextGuidedImageGenerationService("hakurei/waifu-diffusion"));
			serviceImples.put("TrinartStableDiffusion" + postfix, new StableDiffusion060TextGuidedImageGenerationService("naclbit/trinart_stable_diffusion_v2"));
			serviceImples.put("TrinartWaifu" + postfix, new StableDiffusion060TextGuidedImageGenerationService("doohickey/trinart-waifu-diffusion-50-50"));
		}
		{
			var postfix = "DS_0_07_0";
			serviceImples.put("StableDiffusion" + postfix + "SD14", new StableDiffusion070TextGuidedImageGenerationService());
			serviceImples.put("StableDiffusion" + postfix + "SD15", new StableDiffusion070TextGuidedImageGenerationService());
			serviceImples.put("DiscoDiffusion" + postfix, new StableDiffusion070TextGuidedImageGenerationService("sd-dreambooth-library/disco-diffusion-style"));
			serviceImples.put("WaifuDiffusion" + postfix, new StableDiffusion070TextGuidedImageGenerationService("hakurei/waifu-diffusion"));
			serviceImples.put("TrinartStableDiffusion" + postfix, new StableDiffusion070TextGuidedImageGenerationService("naclbit/trinart_stable_diffusion_v2"));
			serviceImples.put("TrinartWaifu" + postfix, new StableDiffusion070TextGuidedImageGenerationService("doohickey/trinart-waifu-diffusion-50-50"));
		}
		{
			var postfix = "DS_0_07_2";
			serviceImples.put("MidjourneyV4" + postfix, new DiffusersTextGuidedImageGenerationService(
				"./procs/diffusers_0_7_2", "prompthero/midjourney-v4-diffusion", "mdjrny-v4 style"));
			serviceImples.put("Ghibli" + postfix, new DiffusersTextGuidedImageGenerationService(
				"./procs/diffusers_0_7_2", "nitrosocke/Ghibli-Diffusion", "ghibli style"));
		}
		{
			var dir = "./procs/diffusers_0_8_1";
			var postfix = "DS_0_08_1";
			addDiffusersTGIG(dir, "StableDiffusion" + postfix + "SD14", "CompVis/stable-diffusion-v1-4");
			addDiffusersTGIG(dir, "StableDiffusion" + postfix + "SD15", "runwayml/stable-diffusion-v1-5");
			addDiffusersTGIG(dir, "DiscoDiffusion" + postfix, "sd-dreambooth-library/disco-diffusion-style");
			addDiffusersTGIG(dir, "WaifuDiffusion" + postfix, "hakurei/waifu-diffusion");
			addDiffusersTGIG(dir, "TrinartStableDiffusion" + postfix, "naclbit/trinart_stable_diffusion_v2");
			addDiffusersTGIG(dir, "ChiyodaMomoTrinartWaifu" + postfix, "V3B4/chiyoda-momo-trinart-waifu-diffusion-50-50");
			addDiffusersTGIG(dir, "MidjourneyV4" + postfix, "prompthero/midjourney-v4-diffusion",
				"mdjrny-v4 style");
			addDiffusersTGIG(dir, "Ghibli" + postfix, "nitrosocke/Ghibli-Diffusion",
				"ghibli style");
		}

		serviceImples.put("StableDiffusionV2", new DiffusersTextGuidedImageGenerationService(
			"./procs/stable_diffusion_v2", "stabilityai/stable-diffusion-2"));

		{
			var dir = "./procs/diffusers_0_9_0";
			var postfix = "DS_0_09_0";
			addDiffusersTGIG(dir, "StableDiffusion" + postfix + "SD14", "CompVis/stable-diffusion-v1-4");
			addDiffusersTGIG(dir, "StableDiffusion" + postfix + "SD15", "runwayml/stable-diffusion-v1-5");
			addDiffusersTGIG(dir, "DiscoDiffusion" + postfix, "sd-dreambooth-library/disco-diffusion-style");
			addDiffusersTGIG(dir, "WaifuDiffusion" + postfix, "hakurei/waifu-diffusion");
			addDiffusersTGIG(dir, "TrinartStableDiffusion" + postfix, "naclbit/trinart_stable_diffusion_v2");
			addDiffusersTGIG(dir, "ChiyodaMomoTrinartWaifu" + postfix, "V3B4/chiyoda-momo-trinart-waifu-diffusion-50-50");
			addDiffusersTGIG(dir, "MidjourneyV4" + postfix, "prompthero/midjourney-v4-diffusion",
				"mdjrny-v4 style");
			addDiffusersTGIG(dir, "Ghibli" + postfix, "nitrosocke/Ghibli-Diffusion",
				"ghibli style");
		}
		{
			var dir = "./procs/diffusers_0_10_2";
			var postfix = "DS_0_10_2";
			addDiffusersTGIG(dir, "StableDiffusion" + postfix + "SD14", "CompVis/stable-diffusion-v1-4");
			addDiffusersTGIG(dir, "StableDiffusion" + postfix + "SD15", "runwayml/stable-diffusion-v1-5");
			addDiffusersTGIG(dir, "DiscoDiffusion" + postfix, "sd-dreambooth-library/disco-diffusion-style");
			addDiffusersTGIG(dir, "WaifuDiffusion" + postfix, "hakurei/waifu-diffusion");
			addDiffusersTGIG(dir, "TrinartStableDiffusion" + postfix, "naclbit/trinart_stable_diffusion_v2");
			addDiffusersTGIG(dir, "ChiyodaMomoTrinartWaifu" + postfix, "V3B4/chiyoda-momo-trinart-waifu-diffusion-50-50");
			addDiffusersTGIG(dir, "MidjourneyV4" + postfix, "prompthero/midjourney-v4-diffusion",
				"mdjrny-v4 style");
			addDiffusersTGIG(dir, "Openjourney" + postfix, "prompthero/openjourney",
				"mdjrny-v4 style");
			addDiffusersTGIG(dir, "Ghibli" + postfix, "nitrosocke/Ghibli-Diffusion",
				"ghibli style");
			addDiffusersTGIG(dir, "CoolJapanDiffusion" + postfix, "alfredplpl/cool-japan-diffusion-for-learning-2-0");
			addDiffusersTGIG(dir, "StableDiffusion" + postfix + "SD20", 
				"stabilityai/stable-diffusion-2", null,
				"run_sd21.py");
			addDiffusersTGIG(dir, "StableDiffusion" + postfix + "SD21",
				"stabilityai/stable-diffusion-2-1", null,
				"run_sd21.py");
		}

		serviceImples.put("StableDiffusionIMSD041", stableDiffusionI2I);
		serviceImples.put("WaifuDiffusionIMSD041", new StableDiffusionTextGuidedImageManipulationService("hakurei/waifu-diffusion"));
		serviceImples.put("TrinartStableDiffusionIMSD041", new StableDiffusionTextGuidedImageManipulationService("naclbit/trinart_stable_diffusion_v2"));
		serviceImples.put("TrinartWaifuIMSD041", new StableDiffusionTextGuidedImageManipulationService("doohickey/trinart-waifu-diffusion-50-50"));
		serviceImples.put("DiscoDiffusionIMSD041", new StableDiffusionTextGuidedImageManipulationService("sd-dreambooth-library/disco-diffusion-style"));

		serviceImples.put("YoloV7", new YoloV7ObjectDetectionService("yolov7.pt"));
		serviceImples.put("YoloV7x", new YoloV7ObjectDetectionService("yolov7x.pt"));
		serviceImples.put("YoloV7w6", new YoloV7ObjectDetectionService("yolov7-w6.pt"));
		serviceImples.put("YoloV7e6", new YoloV7ObjectDetectionService("yolov7-e6.pt"));
		serviceImples.put("YoloV7d6", new YoloV7ObjectDetectionService("yolov7-d6.pt"));
		serviceImples.put("YoloV7e2e", new YoloV7ObjectDetectionService("yolov7-e6e.pt"));

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

	private void addDiffusersTGIG(String procPath, String name, String modelPath, String additionalPrompt,
		String scriptFile){
		serviceImples.put(name, new DiffusersTextGuidedImageGenerationService(
			procPath, modelPath, additionalPrompt, scriptFile));
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

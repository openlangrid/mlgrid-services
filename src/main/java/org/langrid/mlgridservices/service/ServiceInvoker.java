package org.langrid.mlgridservices.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.langrid.mlgridservices.controller.Error;
import org.langrid.mlgridservices.controller.Request;
import org.langrid.mlgridservices.controller.Response;
import org.langrid.mlgridservices.service.group.DalleMiniServiceGroup;
import org.langrid.mlgridservices.service.group.KerasServiceGroup;
import org.langrid.mlgridservices.service.group.LangridServiceGroup;
import org.langrid.mlgridservices.service.group.ServiceGroup;
import org.langrid.mlgridservices.service.group.YoloV5ServiceGroup;
import org.langrid.mlgridservices.service.impl.composite.BindedTextGenerationWithTextToSpeech;
import org.langrid.mlgridservices.service.impl.composite.BindedTextGuidedImageGenerationWithTranslationService;
import org.langrid.mlgridservices.service.impl.composite.TextGenerationWithTranslation;
import org.langrid.mlgridservices.service.impl.test.ProcessFailedExceptionService;
import org.langrid.mlgridservices.service.impl.test.TestGpuPipelineService;
import org.langrid.mlgridservices.service.impl.test.TestGpuService;
import org.langrid.mlgridservices.service.impl.ClipInterrogatorImageToTextService;
import org.langrid.mlgridservices.service.impl.CodeFormerImageToImageConversionService;
import org.langrid.mlgridservices.service.impl.Detectron2ObjectSegmentationService;
import org.langrid.mlgridservices.service.impl.DiffusersTextGuidedImageGenerationService;
import org.langrid.mlgridservices.service.impl.EmpathService;
import org.langrid.mlgridservices.service.impl.ExternalCommandMultimodalTextGenerationService;
import org.langrid.mlgridservices.service.impl.ExternalDockerComposeTextGenerationService;
import org.langrid.mlgridservices.service.impl.ExternalCommandTextGenerationService;
import org.langrid.mlgridservices.service.impl.ExternalTextSimilarityCalculationService;
import org.langrid.mlgridservices.service.impl.ExternalTextSentimentAnalysisService;
import org.langrid.mlgridservices.service.impl.FuguMtTranslationService;
import org.langrid.mlgridservices.service.impl.GoogleTextToSpeechService;
import org.langrid.mlgridservices.service.impl.HelsinkiNlpTranslationService;
import org.langrid.mlgridservices.service.impl.OpenPoseHumanPoseEstimationService;
import org.langrid.mlgridservices.service.impl.ReplCommandTextGeneration;
import org.langrid.mlgridservices.service.impl.ReplCommandTextImageGeneration;
import org.langrid.mlgridservices.service.impl.RealEsrganImageToImageConversionService;
import org.langrid.mlgridservices.service.impl.ReasonSpeechSpeechRecognitionService;
import org.langrid.mlgridservices.service.impl.RinnaJapaneseStableDiffusionTextImageGenerationService;
import org.langrid.mlgridservices.service.impl.SpeechBrainSpeechEmotionRecognitionService;
import org.langrid.mlgridservices.service.impl.StableDiffusion051TextGuidedImageGenerationService;
import org.langrid.mlgridservices.service.impl.StableDiffusion060TextGuidedImageGenerationService;
import org.langrid.mlgridservices.service.impl.StableDiffusion070TextGuidedImageGenerationService;
import org.langrid.mlgridservices.service.impl.StableDiffusionTextGuidedImageManipulationService;
import org.langrid.mlgridservices.service.impl.VallexPipeline;
import org.langrid.mlgridservices.service.impl.VoiceVoxTextToSpeechService;
import org.langrid.mlgridservices.service.impl.StableDiffusionTextGuidedImageGenerationService;
import org.langrid.mlgridservices.service.impl.VoskContinuousSpeechRecognitionService;
import org.langrid.mlgridservices.service.impl.WaifuDiffusionTextImageGenerationService;
import org.langrid.mlgridservices.service.impl.WhisperSpeechRecognitionService;
import org.langrid.mlgridservices.service.impl.YoloV7ObjectDetectionService;
import org.langrid.mlgridservices.service.management.ServiceManagement;
import org.langrid.mlgridservices.util.GpuPool;
import org.langrid.service.ml.interim.management.ServiceEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jp.go.nict.langrid.commons.beanutils.Converter;
import jp.go.nict.langrid.commons.lang.ClassUtil;
import jp.go.nict.langrid.commons.lang.ObjectUtil;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;

@Service
public class ServiceInvoker {
	@PostConstruct
	private void init() {
		// GPU0は共通で使用。GPU1以降がある場合は、GPUの切り替えに対応しているprocで使う。
		if(additionalGpuCount > 0){
			var gpuIds = new int[additionalGpuCount];
			for(int i = 1; i <= additionalGpuCount; i++){
				gpuIds[i - 1] = i;
			}
			ServiceInvokerContext.setGpuPool(new GpuPool(gpuIds));
		}

		// services.ymlを検索してサービス登録
		try{
			new ServiceFinder(Path.of("./procs")).find((si, impl)->{
				serviceImples.put(si.getServiceId(), impl);
				serviceEntries.put(si.getServiceId(), new ServiceEntry(
					si.getServiceId(), findInterface(impl).getSimpleName(),
					si.getDescription(), si.getLicense(), si.getUrl()
				));
			});
		} catch(IOException e){
			throw new RuntimeException(e);
		}

		// serviceImplesにはあるサービスIDに対応する実装クラスを登録する。
		serviceImples.put("CodeFormer", codeFormer);
		serviceImples.put("Empath", empath);
		serviceImples.put("GoogleTTS", googleTts);

		// TextToSpeech
		for(var i = 0; i < 20; i++){
			serviceImples.put(
				String.format("VoiceVox_0_11_4_%02d", i),
				new VoiceVoxTextToSpeechService("./procs/voicevox_0_11_4", "" + i));
		}
		serviceImples.put("Plachtaa/VALL-E-X/cafe|P", new VallexPipeline(
			"./procs/vallex", "bash", "run_pipeline.sh", "cafe"
		));
		serviceImples.put("Plachtaa/VALL-E-X/dingzhen|P", new VallexPipeline(
			"./procs/vallex", "bash", "run_pipeline.sh", "dingzhen"
		));
		serviceImples.put("Plachtaa/VALL-E-X/esta|P", new VallexPipeline(
			"./procs/vallex", "bash", "run_pipeline.sh", "esta"
		));
		serviceImples.put("Plachtaa/VALL-E-X/rosalia|P", new VallexPipeline(
			"./procs/vallex", "bash", "run_pipeline.sh", "rosalia"
		));
		serviceImples.put("Plachtaa/VALL-E-X/seel|P", new VallexPipeline(
			"./procs/vallex", "bash", "run_pipeline.sh", "seel"
		));
		serviceImples.put("Plachtaa/VALL-E-X/yaesakura|P", new VallexPipeline(
			"./procs/vallex", "bash", "run_pipeline.sh", "yaesakura"
		));

		serviceImples.put("HelsinkiNLPOpusMT", new HelsinkiNlpTranslationService());
		serviceImples.put("FuguMT", new FuguMtTranslationService());
		serviceImples.put("ClipInterrogator", new ClipInterrogatorImageToTextService());
		serviceImples.put("OpenPose", new OpenPoseHumanPoseEstimationService());
		serviceImples.put("RealESRGAN", new RealEsrganImageToImageConversionService());
		serviceImples.put("RinnaJapaneseStableDiffusion", rinnaJapaneseStableDiffusion);
		serviceImples.put("SpeechBrainSER", new SpeechBrainSpeechEmotionRecognitionService());
		serviceImples.put("VOSK", new VoskContinuousSpeechRecognitionService());
		serviceImples.put("Whisper", new WhisperSpeechRecognitionService());
		serviceImples.put("ReasonSpeech", new ReasonSpeechSpeechRecognitionService());


		serviceImples.put("WaifuDiffusionDS0_03_0", waifuDiffusion);
		{
			var postfix = "_DS_0_04_1";
			serviceImples.put("StableDiffusion" + postfix, stableDiffusion);
			serviceImples.put("WaifuDiffusion" + postfix, new StableDiffusionTextGuidedImageGenerationService("hakurei/waifu-diffusion"));
			serviceImples.put("TrinartStableDiffusion" + postfix, new StableDiffusionTextGuidedImageGenerationService("naclbit/trinart_stable_diffusion_v2"));
			serviceImples.put("DiscoDiffusion" + postfix, new StableDiffusionTextGuidedImageGenerationService("sd-dreambooth-library/disco-diffusion-style"));
			serviceImples.put("TrinartWaifu" + postfix, new StableDiffusionTextGuidedImageGenerationService("doohickey/trinart-waifu-diffusion-50-50"));
		}
		{
			var postfix = "_DS_0_05_1";
			serviceImples.put("StableDiffusion" + postfix, new StableDiffusion051TextGuidedImageGenerationService());
			serviceImples.put("DiscoDiffusion" + postfix, new StableDiffusion051TextGuidedImageGenerationService("sd-dreambooth-library/disco-diffusion-style"));
			serviceImples.put("WaifuDiffusion" + postfix, new StableDiffusion051TextGuidedImageGenerationService("hakurei/waifu-diffusion"));
			serviceImples.put("TrinartStableDiffusion" + postfix, new StableDiffusion051TextGuidedImageGenerationService("naclbit/trinart_stable_diffusion_v2"));
			serviceImples.put("TrinartWaifu" + postfix, new StableDiffusion051TextGuidedImageGenerationService("doohickey/trinart-waifu-diffusion-50-50"));
		}
		{
			var postfix = "_DS_0_06_0";
			serviceImples.put("StableDiffusion" + postfix + "SD14", new StableDiffusion060TextGuidedImageGenerationService());
			serviceImples.put("StableDiffusion" + postfix + "SD15", new StableDiffusion060TextGuidedImageGenerationService());
			serviceImples.put("DiscoDiffusion" + postfix, new StableDiffusion060TextGuidedImageGenerationService("sd-dreambooth-library/disco-diffusion-style"));
			serviceImples.put("WaifuDiffusion" + postfix, new StableDiffusion060TextGuidedImageGenerationService("hakurei/waifu-diffusion"));
			serviceImples.put("TrinartStableDiffusion" + postfix, new StableDiffusion060TextGuidedImageGenerationService("naclbit/trinart_stable_diffusion_v2"));
			serviceImples.put("TrinartWaifu" + postfix, new StableDiffusion060TextGuidedImageGenerationService("doohickey/trinart-waifu-diffusion-50-50"));
		}
		{
			var postfix = "_DS_0_07_0";
			serviceImples.put("StableDiffusion" + postfix + "SD14", new StableDiffusion070TextGuidedImageGenerationService());
			serviceImples.put("StableDiffusion" + postfix + "SD15", new StableDiffusion070TextGuidedImageGenerationService());
			serviceImples.put("DiscoDiffusion" + postfix, new StableDiffusion070TextGuidedImageGenerationService("sd-dreambooth-library/disco-diffusion-style"));
			serviceImples.put("WaifuDiffusion" + postfix, new StableDiffusion070TextGuidedImageGenerationService("hakurei/waifu-diffusion"));
			serviceImples.put("TrinartStableDiffusion" + postfix, new StableDiffusion070TextGuidedImageGenerationService("naclbit/trinart_stable_diffusion_v2"));
			serviceImples.put("TrinartWaifu" + postfix, new StableDiffusion070TextGuidedImageGenerationService("doohickey/trinart-waifu-diffusion-50-50"));
		}
		{
			var postfix = "_DS_0_07_2";
			serviceImples.put("MidjourneyV4" + postfix, new DiffusersTextGuidedImageGenerationService(
				"./procs/diffusers_0_7_2", "prompthero/midjourney-v4-diffusion", "mdjrny-v4 style"));
			serviceImples.put("Ghibli" + postfix, new DiffusersTextGuidedImageGenerationService(
				"./procs/diffusers_0_7_2", "nitrosocke/Ghibli-Diffusion", "ghibli style"));
		}
		{
			var dir = "./procs/diffusers_0_8_1";
			var postfix = "_DS_0_08_1";
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
			var postfix = "_DS_0_09_0";
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
			var postfix = "_DS_0_10_2";
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
		{
			var dir = "./procs/diffusers_0_14_0";
			var postfix = "_DS_0_14_0";
			addDiffusersTGIG(dir, "CoolJapanDiffusion" + postfix, "alfredplpl/cool-japan-diffusion-for-learning-2-0");
			addDiffusersTGIG(dir, "DiscoDiffusion" + postfix, "sd-dreambooth-library/disco-diffusion-style");
			addDiffusersTGIG(dir, "Ghibli" + postfix, "nitrosocke/Ghibli-Diffusion",
				"ghibli style");
			addDiffusersTGIG(dir, "MidjourneyV4" + postfix, "prompthero/midjourney-v4-diffusion",
				"mdjrny-v4 style");
			addDiffusersTGIG(dir, "Openjourney" + postfix, "prompthero/openjourney",
				"mdjrny-v4 style");
			addDiffusersTGIG(dir, "OpenjourneyV4" + postfix, "prompthero/openjourney-v4");
			addDiffusersTGIG(dir, "PicassoDiffusion" + postfix, "alfredplpl/picasso-diffusion-1-1");
			addDiffusersTGIG(dir, "StableDiffusion" + postfix + "SD14", "CompVis/stable-diffusion-v1-4");
			addDiffusersTGIG(dir, "StableDiffusion" + postfix + "SD15", "runwayml/stable-diffusion-v1-5");
			addDiffusersTGIG(dir, "StableDiffusion" + postfix + "SD20", 
				"stabilityai/stable-diffusion-2", null,
				"run_sd21.py");
			addDiffusersTGIG(dir, "StableDiffusion" + postfix + "SD21",
				"stabilityai/stable-diffusion-2-1", null,
				"run_sd21.py");
			addDiffusersTGIG(dir, "TrinartStableDiffusion" + postfix, "naclbit/trinart_stable_diffusion_v2");
			addDiffusersTGIG(dir, "WaifuDiffusion" + postfix, "hakurei/waifu-diffusion");
		}
		serviceImples.put("StableDiffusion_DS_0_14_0SD21WithFuguMt",
			new BindedTextGuidedImageGenerationWithTranslationService("en", "FuguMT", "StableDiffusion_DS_0_14_0SD21"));
		{
			var dir = "./procs/diffusers_0_19_3";
			var postfix = "_DS_0_19_3";
			addDiffusersTGIG(dir, "SDXLBase" + postfix,
				"stabilityai/stable-diffusion-xl-base-1.0",
				"", "runSDXL.py");
			addDiffusersTGIG(dir, "SDXLBaseWithRefiner" + postfix,
				"stabilityai/stable-diffusion-xl-base-1.0",
				"", "runSDXLWithRefiner.py");

			serviceImples.put("SDXLBase" + postfix + "|P", new ReplCommandTextImageGeneration(
				"./procs/diffusers_0_19_3", "bash", "run_sdxl_base_pipeline.sh",
				"stabilityai/stable-diffusion-xl-base-1.0"));
			serviceImples.put("SDXLBaseWithRefiner" + postfix + "|P", new ReplCommandTextImageGeneration(
				"./procs/diffusers_0_19_3", "bash", "run_sdxl_base_with_refiner_pipeline.sh",
				"stabilityai/stable-diffusion-xl-base-1.0"));
		}

		// Image Manipulation
		{
			var postfix = "_DS_0_04_1";
			serviceImples.put("StableDiffusionIM" + postfix, stableDiffusionI2I);
			serviceImples.put("WaifuDiffusionIM" + postfix, new StableDiffusionTextGuidedImageManipulationService("hakurei/waifu-diffusion"));
			serviceImples.put("TrinartStableDiffusionIM" + postfix, new StableDiffusionTextGuidedImageManipulationService("naclbit/trinart_stable_diffusion_v2"));
			serviceImples.put("TrinartWaifuIM" + postfix, new StableDiffusionTextGuidedImageManipulationService("doohickey/trinart-waifu-diffusion-50-50"));
			serviceImples.put("DiscoDiffusionIM" + postfix, new StableDiffusionTextGuidedImageManipulationService("sd-dreambooth-library/disco-diffusion-style"));
		}

		// Text Sentiment Analysis
		serviceImples.put("DaigoBertJapaneseSentiment", new ExternalTextSentimentAnalysisService(
			"./procs/text_sentiment_analysis_huggingface",
			"daigo/bert-base-japanese-sentiment",
			"cl-tohoku/bert-base-japanese-whole-word-masking"));
		serviceImples.put("KoheiduckBertJapaneseFinetunedSentiment", new ExternalTextSentimentAnalysisService(
			"./procs/text_sentiment_analysis_huggingface",
			"koheiduck/bert-japanese-finetuned-sentiment",
			"cl-tohoku/bert-base-japanese-whole-word-masking"));
	
		// Text Similarity Calculation
		serviceImples.put("USETextSimilarityCalculation", new ExternalTextSimilarityCalculationService("./procs/use", "normal"));
		serviceImples.put("USELargeTextSimilarityCalculation", new ExternalTextSimilarityCalculationService("./procs/use", "large"));
		serviceImples.put("LaBSETextSimilarityCalculation", new ExternalTextSimilarityCalculationService("./procs/labse"));

		serviceImples.put("YoloV7", new YoloV7ObjectDetectionService("yolov7.pt"));
		serviceImples.put("YoloV7x", new YoloV7ObjectDetectionService("yolov7x.pt"));
		serviceImples.put("YoloV7w6", new YoloV7ObjectDetectionService("yolov7-w6.pt"));
		serviceImples.put("YoloV7e6", new YoloV7ObjectDetectionService("yolov7-e6.pt"));
		serviceImples.put("YoloV7d6", new YoloV7ObjectDetectionService("yolov7-d6.pt"));
		serviceImples.put("YoloV7e6e", new YoloV7ObjectDetectionService("yolov7-e6e.pt"));

		serviceImples.put("Detectron2CCR050FPN1x", new Detectron2ObjectSegmentationService("COCO-InstanceSegmentation/mask_rcnn_R_50_FPN_1x.yaml"));
		serviceImples.put("Detectron2CCR050FPN3x", new Detectron2ObjectSegmentationService("COCO-InstanceSegmentation/mask_rcnn_R_50_FPN_3x.yaml"));
		serviceImples.put("Detectron2CCR101FPN3x", new Detectron2ObjectSegmentationService("COCO-InstanceSegmentation/mask_rcnn_R_101_FPN_3x.yaml"));
		serviceImples.put("Detectron2CCX101FPN3x", new Detectron2ObjectSegmentationService("COCO-InstanceSegmentation/mask_rcnn_X_101_32x8d_FPN_3x.yaml"));

		serviceImples.put("JapaneseAlpacaLoRA07b", new ExternalDockerComposeTextGenerationService("./procs/japanese-alpaca-lora", "decapoda-research/llama-7b-hf"));
		serviceImples.put("JapaneseAlpacaLoRA13b", new ExternalDockerComposeTextGenerationService("./procs/japanese-alpaca-lora", "decapoda-research/llama-13b-hf"));
		serviceImples.put("JapaneseAlpacaLoRA30b", new ExternalDockerComposeTextGenerationService("./procs/japanese-alpaca-lora", "decapoda-research/llama-30b-hf"));
//		serviceImples.put("JapaneseAlpacaLoRA65b", new ExternalTextGenerationService("./procs/japanese-alpaca-lora", "decapoda-research/llama-65b-hf"));
		serviceImples.put("Cerebras-GPT2.7B", new ExternalDockerComposeTextGenerationService("./procs/cerebras_gpt", "cerebras/Cerebras-GPT-2.7B"));
		serviceImples.put("Cerebras-GPT6.7B", new ExternalDockerComposeTextGenerationService("./procs/cerebras_gpt", "cerebras/Cerebras-GPT-6.7B"));
		serviceImples.put("MosaicML-MPT7B", new ExternalDockerComposeTextGenerationService("./procs/mosaicml_mpt", "mosaicml/mpt-7b-instruct"));
		serviceImples.put("RWKV-LoRA-Alpaca-Cleaned-Japan", new ExternalDockerComposeTextGenerationService("./procs/rwkv", "/models/RWKV-4-Pile-14B-Instruct-test5-20230329-ctx4096"));

		serviceImples.put("OpenCalmSmall", new ExternalDockerComposeTextGenerationService(
			"./procs/open-calm", "cyberagent/open-calm-small"));
		serviceImples.put("OpenCalmMedium", new ExternalDockerComposeTextGenerationService(
			"./procs/open-calm", "cyberagent/open-calm-medium"));
		serviceImples.put("OpenCalmLarge", new ExternalDockerComposeTextGenerationService(
			"./procs/open-calm", "cyberagent/open-calm-large"));
		serviceImples.put("OpenCalm1B", new ExternalDockerComposeTextGenerationService(
			"./procs/open-calm", "cyberagent/open-calm-1b"));
		serviceImples.put("OpenCalm3B", new ExternalDockerComposeTextGenerationService(
			"./procs/open-calm", "cyberagent/open-calm-3b"));
		serviceImples.put("OpenCalm7B", new ExternalDockerComposeTextGenerationService(
			"./procs/open-calm", "cyberagent/open-calm-7b"));

		serviceImples.put("RinnaJapaneseGPT3.6B", new ExternalDockerComposeTextGenerationService(
			"./procs/japanese-gpt-neox", "rinna/japanese-gpt-neox-3.6b"));
		serviceImples.put("RinnaJapaneseGPT3.6B-Instruction-sft", new ExternalDockerComposeTextGenerationService(
			"./procs/japanese-gpt-neox", "rinna/japanese-gpt-neox-3.6b-instruction-sft",
			"generate_sft.py"));
		serviceImples.put("RinnaJapaneseGPT3.6B-Instruction-sft-v2", new ExternalDockerComposeTextGenerationService(
			"./procs/japanese-gpt-neox", "rinna/japanese-gpt-neox-3.6b-instruction-sft-v2",
			"generate_sft_v2.py"));
		serviceImples.put("RinnaJapaneseGPT3.6B-Instruction-ppo", new ExternalDockerComposeTextGenerationService(
			"./procs/japanese-gpt-neox", "rinna/japanese-gpt-neox-3.6b-instruction-ppo",
			"generate_ppo.py"));
		serviceImples.put("RinnaBilingualGPT4B", new ExternalCommandTextGenerationService(
			"./procs/rinna-bilingual-gpt-neox", "/bin/bash run_gpt.sh", "rinna/bilingual-gpt-neox-4b"));
		serviceImples.put("RinnaBilingualGPT4BInstructionSft", new ExternalCommandTextGenerationService(
			"./procs/rinna-bilingual-gpt-neox", "/bin/bash run_sft_ppo.sh", "rinna/bilingual-gpt-neox-4b-instruction-sft"));
		serviceImples.put("RinnaBilingualGPT4BInstructionPpo", new ExternalCommandTextGenerationService(
			"./procs/rinna-bilingual-gpt-neox", "/bin/bash run_sft_ppo.sh", "rinna/bilingual-gpt-neox-4b-instruction-ppo"));
		serviceImples.put("MetaLLama2-7B", new ExternalCommandTextGenerationService(
			"./procs/meta-llama2", "/bin/bash run.sh", "meta-llama/Llama-2-7b-hf"));
		serviceImples.put("MetaLLama2-7BChat", new ExternalCommandTextGenerationService(
			"./procs/meta-llama2", "/bin/bash run.sh", "meta-llama/Llama-2-7b-chat-hf"));
		serviceImples.put("MetaLLama2-13B", new ExternalCommandTextGenerationService(
			"./procs/meta-llama2", "/bin/bash run.sh", "meta-llama/Llama-2-13b-hf"));
		serviceImples.put("MetaLLama2-13BChat", new ExternalCommandTextGenerationService(
			"./procs/meta-llama2", "/bin/bash run.sh", "meta-llama/Llama-2-13b-chat-hf"));
		serviceImples.put("StabilityAIJapaneseStableLMBaseAlpha-7B", new ExternalCommandTextGenerationService(
			"./procs/stabilityai_japanese_stablelm_alpha", "/bin/bash run_base.sh", "stabilityai/japanese-stablelm-base-alpha-7b"));
		serviceImples.put("StabilityAIJapaneseStableLMInstructAlpha-7B", new ExternalCommandTextGenerationService(
			"./procs/stabilityai_japanese_stablelm_alpha", "/bin/bash run_instruct.sh", "stabilityai/japanese-stablelm-instruct-alpha-7b"));
		serviceImples.put("LightblueJapaneseMpt7B", new ExternalCommandTextGenerationService(
			"./procs/lightblue_japanese_mpt", "/bin/bash run.sh",
			"lightblue/japanese-mpt-7b"));
		serviceImples.put("LineJapaneseLargeLM1.7B", new ExternalCommandTextGenerationService(
			"./procs/line_japanese_large_lm", "/bin/bash run.sh",
			"line-corporation/japanese-large-lm-1.7b"));
		serviceImples.put("LineJapaneseLargeLM1.7BInstractionSft", new ExternalCommandTextGenerationService(
			"./procs/line_japanese_large_lm", "/bin/bash run_sft.sh",
			"line-corporation/japanese-large-lm-1.7b-instruction-sft"));
		serviceImples.put("LineJapaneseLargeLM3.6B", new ExternalCommandTextGenerationService(
			"./procs/line_japanese_large_lm", "/bin/bash run.sh",
			"line-corporation/japanese-large-lm-3.6b"));
		serviceImples.put("LineJapaneseLargeLM3.6BInstructionSft", new ExternalCommandTextGenerationService(
			"./procs/line_japanese_large_lm", "/bin/bash run_sft.sh",
			"line-corporation/japanese-large-lm-3.6b-instruction-sft"));
		serviceImples.put("StockmarkGptNeoxJapanese1.4B", new ExternalCommandTextGenerationService(
			"./procs/stockmark_gpt_neox_japanese", "/bin/bash run.sh",
			"stockmark/gpt-neox-japanese-1.4b"));
		serviceImples.put("LMSYSVicuna7B-v1.1", new ExternalCommandTextGenerationService(
			"./procs/lmsys_vicuna", "/bin/bash run.sh",
			"lmsys/vicuna-7b-v1.1"));
		serviceImples.put("LMSYSVicuna7B-v1.3", new ExternalCommandTextGenerationService(
			"./procs/lmsys_vicuna", "/bin/bash run.sh",
			"lmsys/vicuna-7b-v1.3"));
		serviceImples.put("LMSYSVicuna7B-v1.5", new ExternalCommandTextGenerationService(
			"./procs/lmsys_vicuna", "/bin/bash run.sh",
			"lmsys/vicuna-7b-v1.5"));
		serviceImples.put("LMSYSVicuna7B-v1.5-16k", new ExternalCommandTextGenerationService(
			"./procs/lmsys_vicuna", "/bin/bash run.sh",
			"lmsys/vicuna-7b-v1.5-16k"));
		serviceImples.put("LMSYSVicuna13B-v1.1", new ExternalCommandTextGenerationService(
			"./procs/lmsys_vicuna", "/bin/bash run.sh",
			"lmsys/vicuna-13b-v1.1"));
		serviceImples.put("LMSYSVicuna13B-v1.3", new ExternalCommandTextGenerationService(
			"./procs/lmsys_vicuna", "/bin/bash run.sh",
			"lmsys/vicuna-13b-v1.3"));
		serviceImples.put("LMSYSVicuna13B-v1.5", new ExternalCommandTextGenerationService(
			"./procs/lmsys_vicuna", "/bin/bash run.sh",
			"lmsys/vicuna-13b-v1.5"));
		serviceImples.put("LMSYSVicuna13B-v1.5-16k", new ExternalCommandTextGenerationService(
			"./procs/lmsys_vicuna", "/bin/bash run.sh",
			"lmsys/vicuna-13b-v1.5-16k"));
		serviceImples.put("Qwen7BChat", new ExternalCommandTextGenerationService(
			"./procs/qwenlm", "/bin/bash run.sh",
			"Qwen/Qwen-7B-Chat"));
		serviceImples.put("Qwen7B|P", new ReplCommandTextGeneration(
			"./procs/qwenlm", "bash", "run_completion_pipeline.sh",
			"Qwen/Qwen-7B"));
		serviceImples.put("Qwen7BChat|P", new ReplCommandTextGeneration(
			"./procs/qwenlm", "bash", "run_chat_pipeline.sh",
			"Qwen/Qwen-7B-Chat"));
		serviceImples.put("MatsuoLabWeblab7B", new ExternalCommandTextGenerationService(
			"./procs/matsuolab_weblab", "/bin/bash run.sh",
			"matsuo-lab/weblab-10b"));
		serviceImples.put("MatsuoLabWeblab7BInstruct", new ExternalCommandTextGenerationService(
			"./procs/matsuolab_weblab", "/bin/bash run.sh",
			"matsuo-lab/weblab-10b-instruction-sft"));
		serviceImples.put("MatsuoLabWeblab7BInstruct|P", new ReplCommandTextGeneration(
			"./procs/matsuolab_weblab", "bash", "run_pipeline.sh",
			"matsuo-lab/weblab-10b-instruction-sft"));
/* services.ymlに移行
		serviceImples.put("ELYZAJapaneseLlama2-7b|P", new ReplCommandTextGeneration(
			"./procs/ELYZA-japanese-Llama-2", "bash", "run_completion_repl.sh",
			"elyza/ELYZA-japanese-Llama-2-7b"));
		serviceImples.put("ELYZAJapaneseLlama2-7bInstruct|P", new ReplCommandTextGeneration(
			"./procs/ELYZA-japanese-Llama-2", "bash", "run_completion_repl.sh",
			"elyza/ELYZA-japanese-Llama-2-7b-instruct"));
		serviceImples.put("ELYZAJapaneseLlama2-7bFast|P", new ReplCommandTextGeneration(
			"./procs/ELYZA-japanese-Llama-2", "bash", "run_completion_repl.sh",
			"elyza/ELYZA-japanese-Llama-2-7b-fast"));
		serviceImples.put("ELYZAJapaneseLlama2-7bFastInstruct|P", new ReplCommandTextGeneration(
			"./procs/ELYZA-japanese-Llama-2", "bash", "run_completion_repl.sh",
			"elyza/ELYZA-japanese-Llama-2-7b-fast-instruct"));
*/
		serviceImples.put("StabilityAiStableBeluga7b|P", new ReplCommandTextGeneration(
			"./procs/stabilityai_StableBeluga", "bash", "run_completion_repl.sh",
			"stabilityai/StableBeluga-7B"));
		serviceImples.put("StabilityAiStableBeluga13b|P", new ReplCommandTextGeneration(
			"./procs/stabilityai_StableBeluga", "bash", "run_completion_repl.sh",
			"stabilityai/StableBeluga-13B"));


		serviceImples.put("StabilityAIStableCodeCompletionAlpha3b|P", new ReplCommandTextGeneration(
			"./procs/stabilityai_stablecode_alpha", "bash", "run_completion_pipeline.sh",
			"stabilityai/stablecode-completion-alpha-3b"));
		serviceImples.put("StabilityAIStableCodeCompletionAlpha3b4k|P", new ReplCommandTextGeneration(
			"./procs/stabilityai_stablecode_alpha", "bash", "run_completion_pipeline.sh",
			"stabilityai/stablecode-completion-alpha-3b-4k"));
		serviceImples.put("StabilityAIStableCodeInstructAlpha3b|P", new ReplCommandTextGeneration(
			"./procs/stabilityai_stablecode_alpha", "bash", "run_completion_pipeline.sh",
			"stabilityai/stablecode-instruct-alpha-3b"));

		serviceImples.put("MetaCodeLlama7b|P", new ReplCommandTextGeneration(
			"./procs/meta-codellama", "bash", "run_completion_pipeline.sh",
			"codellama/CodeLlama-7b-hf"));
		serviceImples.put("MetaCodeLlama13b|P", new ReplCommandTextGeneration(
			"./procs/meta-codellama", "bash", "run_completion_pipeline.sh",
			"codellama/CodeLlama-13b-hf"));
		serviceImples.put("MetaCodeLlama34b|P", new ReplCommandTextGeneration(
			"./procs/meta-codellama", "bash", "run_completion_pipeline.sh",
			"codellama/CodeLlama-34b-hf"));
		serviceImples.put("MetaCodeLlama7bPython|P", new ReplCommandTextGeneration(
			"./procs/meta-codellama", "bash", "run_completion_pipeline.sh",
			"codellama/CodeLlama-7b-Python-hf"));
		serviceImples.put("MetaCodeLlama13bPython|P", new ReplCommandTextGeneration(
			"./procs/meta-codellama", "bash", "run_completion_pipeline.sh",
			"codellama/CodeLlama-13b-Python-hf"));
		serviceImples.put("MetaCodeLlama34bPython|P", new ReplCommandTextGeneration(
			"./procs/meta-codellama", "bash", "run_completion_pipeline.sh",
			"codellama/CodeLlama-34b-Python-hf"));
		serviceImples.put("MetaCodeLlama7bInstruct|P", new ReplCommandTextGeneration(
			"./procs/meta-codellama", "bash", "run_completion_pipeline.sh",
			"codellama/CodeLlama-7b-Instruct-hf"));
		serviceImples.put("MetaCodeLlama13bInstruct|P", new ReplCommandTextGeneration(
			"./procs/meta-codellama", "bash", "run_completion_pipeline.sh",
			"codellama/CodeLlama-13b-Instruct-hf"));
		serviceImples.put("MetaCodeLlama34bInstruct|P", new ReplCommandTextGeneration(
			"./procs/meta-codellama", "bash", "run_completion_pipeline.sh",
			"codellama/CodeLlama-34b-Instruct-hf"));


		serviceImples.put("RinnaBilingualGptNeox4BMiniGPT4", new ExternalCommandMultimodalTextGenerationService(
			"./procs/rinna-bilingual-gpt-neox-minigpt4", "/bin/bash run.sh",
			"rinna/bilingual-gpt-neox-4b"));
		serviceImples.put("StabilityAIJapaneseInstructBlipAlpha", new ExternalCommandMultimodalTextGenerationService(
			"./procs/stabilityai_japanese_stablelm_alpha", "/bin/bash run_instruct_blip.sh",
			"stabilityai/japanese-instructblip-alpha"));


		// composite
		serviceImples.put("JapaneseAlpacaLoRA07bWithVoiceVox_0_11_4_08", new BindedTextGenerationWithTextToSpeech(
			"JapaneseAlpacaLoRA07b", "VoiceVox_0_11_4_08"));
		serviceImples.put("JapaneseAlpacaLoRA13bWithVoiceVox_0_11_4_08", new BindedTextGenerationWithTextToSpeech(
			"JapaneseAlpacaLoRA13b", "VoiceVox_0_11_4_08"));
		serviceImples.put("TextGenerationWithTranslation", new TextGenerationWithTranslation());


		serviceImples.put("ProcessFailedExceptionService", new ProcessFailedExceptionService());
		serviceImples.put("TestGpuPipelineService", new TestGpuPipelineService());
		serviceImples.put("TestGpuService", new TestGpuService());


		// serviceGroupsは共通のprefixを持つサービス群をまとめたサービスグループを登録する。
		serviceGroups.put("DalleMini", dalleMiniServices);
		serviceGroups.put("Keras", kerasServices);
		serviceGroups.put("Langrid", langridServices);
		serviceGroups.put("YoloV5", yoloV5Services);


		// serviceEntriesを完成させてServiceManagementを生成し登録
		for(var kv : serviceImples.entrySet()){
			serviceEntries.putIfAbsent(
				kv.getKey(),
				new ServiceEntry(kv.getKey(), findInterface(kv.getValue()).getSimpleName())
				);
		}
		for(var g : serviceGroups.values()){
			for(var p : g.listServices()){
				var sid = p.getFirst();
				var stype = p.getSecond().getSimpleName();
				serviceEntries.put(sid, new ServiceEntry(sid, stype));
			}
		}
		serviceImples.put("ServiceManagement", new ServiceManagement(serviceEntries, serviceImples));
	}

	private Class<?> findInterface(Object service){
		var clz = service.getClass();
		while(clz != null){
			for(var intf : clz.getInterfaces()){
				if(intf.equals(CompositeService.class)) continue;
				if(intf.getName().startsWith("java.")) continue;
				if(intf.getName().startsWith("javax.")) continue;
				return intf;
			}
			clz = clz.getSuperclass();
		}
		throw new RuntimeException("no interfaces found for class " + service.getClass());
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
		var sf = new ServiceFactory(){
			public Object create(String serviceId){
				// serviceIdに対応する実装クラスを探す
				var s = serviceImples.get(serviceId);
				if(s == null){
					// 実装クラスがなければグループを探す
					var g = serviceGroups.get(serviceId);
					if(g == null) {
						// 見つからなければ前方一致で検索
						for(var e : serviceGroups.entrySet()) {
							if(!serviceId.startsWith(e.getKey())) continue;
							g = e.getValue();
						}
					}
					if(g == null) return null;
					s = g.get(serviceId);
				}
				return s;
			}
		};
		ServiceInvokerContext.initialize(
			sf, invocation.getHeaders(), "execution", "ServiceInvoker.invoke", new Object[]{serviceId, invocation});
		Object result = null;
		Throwable exception = null;
		Response r = null;
		try{
			ServiceInvokerContext.start("call", serviceId + "." + invocation.getMethod(), invocation.getArgs());
			try{
				var s = sf.create(serviceId);
				if(s == null){
					throw new ProcessFailedException("service " + serviceId + " not found.");
				}
				System.out.printf("[invokeService] %s -> %s%n", serviceId, s);
				var mn = invocation.getMethod();
				var m = ClassUtil.findMethod(s.getClass(), mn, invocation.getArgs().length);
				if(m == null){
					throw new NoSuchMethodException(String.format("Failed to find %s.%s(%d args)", serviceId, mn, invocation.getArgs().length));
				}
				var args = c.convertEachElement(invocation.getArgs(), m.getParameterTypes());
				result = ObjectUtil.invoke(s, mn, args);
				ServiceInvokerContext.finishWithResult(result);
			} catch(InvocationTargetException e){
				exception = e.getTargetException();
				ServiceInvokerContext.finishWithException(exception);
			} catch(Throwable e){
				exception = e;
				ServiceInvokerContext.finishWithException(e);
			}
		} finally{
			if(result != null){
				var span = ServiceInvokerContext.finalizeWithResult(result);
				r = new Response(result);
				r.putHeader("trace", span);
			} else {
				if(exception == null){
					exception = new RuntimeException("no results");
				}
				var span = ServiceInvokerContext.finalizeWithException(exception);
				r = new Response(new Error(
					"error", exception.toString()));
				r.putHeader("trace", span);
				exception.printStackTrace(System.err);
			}
		}
		return r;
	}

	@Value("${mlgrid.additionalGpuCount}")
	private int additionalGpuCount;

	private Converter c = new Converter();
	private Map<String, ServiceGroup> serviceGroups = new HashMap<>();
	private Map<String, Object> serviceImples = new HashMap<>();
	private Map<String, ServiceEntry> serviceEntries = new HashMap<>();

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
	private KerasServiceGroup kerasServices;
	@Autowired
	private YoloV5ServiceGroup yoloV5Services;
	@Autowired
	private DalleMiniServiceGroup dalleMiniServices;
}

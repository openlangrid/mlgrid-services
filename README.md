# 機械学習サービス基盤 - mlgrid-services

mlgrid-servicesは、様々な機械学習ソフトウェアをWebサービスとして提供する機械学習サービス基盤です。
Java17およびSpringbootを使って開発されています。

現在以下の17種類のサービスインタフェースが定義され、149のサービスが実装されています。以下はインタフェースの一覧と、各サービスに使用されている学習モデルや外部サービスです。学習モデルを利用するための環境はDockerコンテナとして構築されています。[procsディレクトリ](https://github.com/openlangrid/mlgrid-services/tree/master/procs)以下にそれぞれのモデル毎の設定ファイルや実行スクリプト(bash, pythonなど)が格納されています。各モデルの利用により生成されたデータの扱いは、それぞれの利用規約を参照してください。

* 機械翻訳([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/TranslationService.java))
    * [FuguMT](https://huggingface.co/staka/fugumt-en-ja)
    * [HelsinkiNLPOpusMT](https://huggingface.co/Helsinki-NLP/opus-mt-en-jap)
* テキスト生成([インタフェース定義](https://github.com/openlangrid/mlgrid-services/blob/master/src/main/java/org/langrid/service/ml/interim/TextGenerationService.java))
    * [Cerebras-GPT](https://huggingface.co/cerebras)
    * [JapaneseAlpacaLoRA](https://github.com/kunishou/Japanese-Alpaca-LoRA)
    * [MosaicML-MPT](https://huggingface.co/mosaicml/mpt-7b)
    * [OpenCalm](https://huggingface.co/cyberagent/open-calm-small/blob/main/README.md)
    * [RinnaJapaneseGPT](https://huggingface.co/rinna/japanese-gpt-neox-3.6b)
    * [RWKV](https://huggingface.co/BlinkDL/rwkv-4-pile-14b)([LoRA-Alpaca-Cleaned-Japan](https://huggingface.co/shi3z/RWKV-LM-LoRA-Alpaca-Cleaned-Japan))
* テキスト生成音声合成([インタフェース定義](https://github.com/openlangrid/mlgrid-services/blob/master/src/main/java/org/langrid/service/ml/interim/TextGenerationWithTextToSpeechService.java))
* テキスト感情分析([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/TextSentimentAnalysisService.java))
    * [DaigoBertJapaneseSentiment](https://huggingface.co/daigo/bert-base-japanese-sentiment)
    * [KoheiduckBertJapaneseFinetunedSentiment](https://huggingface.co/koheiduck/bert-japanese-finetuned-sentiment)
* テキスト類似度計算([インタフェース定義](https://github.com/openlangrid/mlgrid-services/blob/master/src/main/java/org/langrid/service/ml/interim/TextSimilarityCalculationService.java))
    * [Universal Sentence Encoder](https://tfhub.dev/google/universal-sentence-encoder-multilingual/3)
    * [Language-agnostic Bert Sentence Embedding](https://tfhub.dev/google/LaBSE/2)
* 画像生成([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/TextGuidedImageGenerationService.java))
    * [ChiyodamomoTrinartWaifu50-50](https://huggingface.co/V3B4/chiyoda-momo-trinart-waifu-diffusion-50-50)
    * [Cool Japan Diffusion for learning 2.0](https://huggingface.co/alfredplpl/cool-japan-diffusion-for-learning-2-0)
    * [Dalle Mega](https://huggingface.co/dalle-mini/dalle-mega)
    * [Dalle Mini](https://huggingface.co/dalle-mini/dalle-mini)
    * [Disco Diffusion style on Stable Diffusion via Dreamboot](https://huggingface.co/sd-dreambooth-library/disco-diffusion-style)
    * [Ghibli Diffusion](https://huggingface.co/nitrosocke/Ghibli-Diffusion)
    * [Japanese Stable Diffusion](https://huggingface.co/rinna/japanese-stable-diffusion)
    * [Midjourney style](https://huggingface.co/prompthero/midjourney-v4-diffusion)
    * [Openjourney](https://huggingface.co/prompthero/openjourney-v4)
    * [Picasso Diffusion 1.1](https://huggingface.co/aipicasso/picasso-diffusion-1-1)
    * [Stable Diffusion v1-4](https://huggingface.co/CompVis/stable-diffusion-v1-4)
    * [Stable Diffusion v1-5](https://huggingface.co/runwayml/stable-diffusion-v1-5)
    * [Stable Diffusion v2](https://huggingface.co/stabilityai/stable-diffusion-2)
    * [Stable Diffusion v2-1](https://huggingface.co/stabilityai/stable-diffusion-2-1)
    * [Trinart Stable Diffusion v2](https://huggingface.co/naclbit/trinart_stable_diffusion_v2)
    * [Trinart Waifu Diffusion 50-50](https://huggingface.co/doohickey/trinart-waifu-diffusion-50-50)
    * [waifu-diffusion v1.4](https://huggingface.co/hakurei/waifu-diffusion)
* テキスト指示での画像編集([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/TextGuidedImageManipulationService.java))
    * 画像生成のモデルの一部を利用
* 画像変換([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/ImageConversionService.java))
    * [CodeFormer](https://github.com/sczhou/CodeFormer)
    * [RealESRGAN](https://github.com/xinntao/Real-ESRGAN)
* 画像テキスト化([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/ImageToTextConversionService.java))
    * [clip-interrogator](https://github.com/pharmapsychotic/clip-interrogator)
* 画像分類([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/ImageClassificationService.java))
    * [Keras](https://www.tensorflow.org/api_docs/python/tf/keras)
* 物体検出([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/ObjectDetectionService.java))
    * [YoloV5](https://github.com/ultralytics/yolov5)
    * [YoloV7](https://github.com/WongKinYiu/yolov7)
* セグメンテーション([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/ImageSegmentationService.java))
    * [Detectron](https://github.com/facebookresearch/detectron2)
* 姿勢推定([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/HumanPoseEstimation3dService.java))
    * [OpenPose](https://github.com/CMU-Perceptual-Computing-Lab/openpose)
* 音声感情認識([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/SpeechEmotionRecognitionService.java))
* 音声認識([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/SpeechRecognitionService.java))
    * [SpeechBrain](https://github.com/speechbrain/speechbrain)
* リアルタイム音声認識([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/ContinuousSpeechRecognitionService.java))
    * [VOSK](https://alphacephei.com/vosk/server)
* 音声合成([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/TextToSpeechService.java))
    * [Google Cloud Text-to-Speech](https://cloud.google.com/text-to-speech)
    * [VoiceVox](https://github.com/VOICEVOX/voicevox_core/)

## 起動方法

### 必要なソフトウェア

以下のソフトウェアを使用しています。mlgrid-serviceを起動するシステムに、あらかじめインストールしておく必要があります。

* JDK 17 ([Temurin](https://adoptium.net/temurin/releases/))
* Docker 20.10 + docker-compose 1.29 ([Docker Engine](https://docs.docker.com/engine/), [Docker Compose](https://docs.docker.com/compose/))

### ビルド方法

JDK17をインストールした状態で、以下のコマンドを実行してください。

```bash
git clone https://github.com/openlangrid/mlgrid-services/
cd mlgrid-services
./gradlew build -x test
```

### 起動方法

まず、設定ファイルをコピーしてください。

```bash
cp ./src/main/resources/application.yml.sample ./src/main/resources/application.yml
```

次に、コピーした設定ファイルの環境に応じて編集してください。

```yaml
server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: ${CONTEXT_PATH:/mlgrid-services}

services:
  langrid:
    url: ${LANGRID_URL:https://langrid.org/service_manager/invoker/}
    username: ${LANGRID_USERNAME:langrid-user}
    password: ${LANGRID_PASSWORD:langrid-pass}

  keras:
    docker-service-name: ${KERAS_SERVICE_NAME:keras-gpu}

  empath:
    endpoint: ${EMPATH_ENDPOINT:https://api.webempath.net/v2/analyzeWav}
    api-key: ${EMPATH_APIKEY:empath-api-key}

  google:
    tts-api-key-file: ${GOOGLE_TTS_APIKEY:./google-tts-key.json}
```

[言語グリッド](https://langrid.org/), [Empath](https://webempath.net/lp-jpn/), [Google Cloud TTS](https://cloud.google.com/text-to-speech) を利用する場合、それぞれのURLや認証情報を変更してください。

Kerasを用いた画像認識でCPUを使用する場合は、keras.docker-service-nameをkeras-cpuに変更してください。

ファイル変更後、以下のコマンドを実行すると、mlgrid-servicesが起動します。

```bash
java -jar ./build/libs/mlgrid-services-0.0.1-SNAPSHOT.jar
```



## Acknowledgements
このソフトウェアは、科研費19K20243の助成を受けた研究において作成されたものです。

## References
* 中口孝雄. 機械学習システムへの複合サービス技術の適用. 電子情報通信学会技術研究報告; 信学技報, 2019, 119.178: 39-40.
* 中口孝雄. 機械学習サービスを登録・提供するサービス基盤の構築に向けて. NAIS Journal, 2021, 15: 66-73.

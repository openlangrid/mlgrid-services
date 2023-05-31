# 機械学習サービス基盤 - mlgrid-services

mlgrid-serviceは、様々な機械学習ソフトウェアをサービスとして提供する機械学習サービス基盤です。

現在以下の17種類のサービスインタフェースが定義され、149のサービスが実装されています。

* 機械翻訳([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/TranslationService.java))
    * [FuguMT](https://huggingface.co/staka/fugumt-en-ja)
    * [HelsinkiNLPOpusMT](https://huggingface.co/Helsinki-NLP/opus-mt-en-jap)
* テキスト生成([インタフェース定義](https://github.com/openlangrid/mlgrid-services/blob/master/src/main/java/org/langrid/service/ml/interim/TextGenerationService.java))
    * Cerebras-GPT
    * JapaneseAlpacaLoRA
    * MosaicML-MPT
    * OpenCalm
    * RinnaJapaneseGPT
    * RWKV(LoRA-Alpaca-Cleaned-Japan)
* テキスト生成音声合成([インタフェース定義](https://github.com/openlangrid/mlgrid-services/blob/master/src/main/java/org/langrid/service/ml/interim/TextGenerationWithTextToSpeechService.java))
* テキスト感情分析([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/TextSentimentAnalysisService.java))
* テキスト類似度計算([インタフェース定義](https://github.com/openlangrid/mlgrid-services/blob/master/src/main/java/org/langrid/service/ml/interim/TextSimilarityCalculationService.java))
* 画像生成([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/TextGuidedImageGenerationService.java))
* テキスト指示での画像編集([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/TextGuidedImageManipulationService.java))
* 画像変換([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/ImageConversionService.java))
* 画像テキスト化([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/ImageToTextConversionService.java))
* 画像分類([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/ImageClassificationService.java))
* 物体検出([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/ObjectDetectionService.java))
* セグメンテーション([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/ImageSegmentationService.java))
* 姿勢推定([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/HumanPoseEstimation3dService.java))
* 音声感情認識([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/SpeechEmotionRecognitionService.java))
* 音声認識([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/SpeechRecognitionService.java))
* リアルタイム音声認識([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/ContinuousSpeechRecognitionService.java))
* 音声合成([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/TextToSpeechService.java))

## Acknowledgements
このソフトウェアは、科研費19K20243の助成を受けた研究において作成されたものです。

## References
* 中口孝雄. 機械学習システムへの複合サービス技術の適用. 電子情報通信学会技術研究報告; 信学技報, 2019, 119.178: 39-40.
* 中口孝雄. 機械学習サービスを登録・提供するサービス基盤の構築に向けて. NAIS Journal, 2021, 15: 66-73.

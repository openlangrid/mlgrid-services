# 機械学習サービス基盤 - mlgrid-services

mlgrid-serviceは、様々な機械学習ソフトウェアをサービスとして提供する機械学習サービス基盤です。

現在以下の17種類のサービスインタフェースが定義され、149のサービスが実装されています。

* 機械翻訳([インタフェース定義](https://github.com/openlangrid/mlgrid/blob/master/org.langrid.service.ml/src/main/java/org/langrid/service/ml/TranslationService.java))
* テキスト生成([インタフェース定義](https://github.com/openlangrid/mlgrid-services/blob/master/src/main/java/org/langrid/service/ml/interim/TextGenerationService.java))
* テキスト生成音声合成([インタフェース定義](https://github.com/openlangrid/mlgrid-services/blob/master/src/main/java/org/langrid/service/ml/interim/TextGenerationWithTextToSpeechService.java))
* テキスト感情分析
* テキスト類似度計算
* 画像生成
* テキスト指示での画像編集
* 画像変換
* 画像テキスト化
* 画像分類
* 物体検出
* セグメンテーション
* 姿勢推定
* 音声感情認識
* 音声認識
* リアルタイム音声認識
* 音声合成

## Acknowledgements
このソフトウェアは、科研費19K20243の助成を受けた研究において作成されたものです。

## References

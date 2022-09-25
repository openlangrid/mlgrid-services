# 使い方

1. モデルのダウンロード https://alphacephei.com/vosk/models
1. モデルの展開(unzip)
1. vosk-server起動
    ```
    docker run -d --name vosk-ja --rm \
      -p 2700:2700 \
      -v $(pwd)/models:/opt/vosk-server/models \
      -w /opt/vosk-server/websocket \
      alphacep/kaldi-vosk-server:latest \
      python3 asr_server.py ${展開したディレクトリ: /opt/vosk-server/models/vosk-model-small-ja-0.22}
    ```

#!/bin/bash

MODEL=${1:-"models/vosk-model-small-ja-0.22"}

docker run -d --name vosk --rm \
  -p 2700:2700 \
  -v $(pwd)/models:/opt/vosk-server/models \
  -w /opt/vosk-server/websocket \
  alphacep/kaldi-vosk-server:latest \
  python3 asr_server.py /opt/vosk-server/$MODEL

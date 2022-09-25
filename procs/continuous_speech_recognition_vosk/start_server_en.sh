#!/bin/bash

MODEL=${1:-"models/vosk-model-small-en-us-0.15"}

docker run -d --rm \
  -p 2701:2700 \
  -v $(pwd)/models:/opt/vosk-server/models \
  -w /opt/vosk-server/websocket \
  alphacep/kaldi-vosk-server:latest \
  python3 asr_server.py /opt/vosk-server/$MODEL

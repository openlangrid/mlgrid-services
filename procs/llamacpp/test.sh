#!/bin/bash
MODEL=karakuri-lm-70b-chat-v0.1-q5_K_S.gguf
URL=https://huggingface.co/mmnga/karakuri-lm-70b-chat-v0.1-gguf/resolve/main/$MODEL?download=true

if [ ! -e ./models/$MODEL ]; then
  wget $URL -O ./models/$MODEL
fi

docker compose run -T --rm service python test.py $@

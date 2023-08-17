#!/bin/bash

if [ ! -e ./files/checkpoint.pth ]; then
  mkdir -p files
  pushd files
  wget https://huggingface.co/rinna/bilingual-gpt-neox-4b-minigpt4/resolve/main/customized_mini_gpt4.py
  wget https://huggingface.co/rinna/bilingual-gpt-neox-4b-minigpt4/resolve/main/checkpoint.pth
fi

docker compose run --rm service python run.py $@

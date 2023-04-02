#!/bin/bash

MODEL="${1:-cerebras/Cerebras-GPT-2.7B}"
shift
INPUT_PATH="${1:-./sample.txt}"
shift
OUT_PATH_PREFIX="${1:-./sample}"
shift

docker-compose run --rm service python run.py \
  --utterancePath ${INPUT_PATH} \
  --outPathPrefix ${OUT_PATH_PREFIX} \
  --model ${MODEL} $*

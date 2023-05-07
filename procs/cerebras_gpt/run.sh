#!/bin/bash

# models:
#  cerebras/Cerebras-GPT-2.7B
#  cerebras/Cerebras-GPT-6.7B
MODEL="${1:-cerebras/Cerebras-GPT-2.7B}"
shift
INPUT_PATH="${1:-./sample/input.txt}"
shift
OUTPUT_PATH="${1:-./sample/output.txt}"
shift
TEMPERATURE="${1:-0.7}"   # 0.1
shift
TOP_P="${1:-0.9}"  # 0.75
shift
TOP_K="${1:-40}"


docker-compose run --rm service python run.py \
  --model ${MODEL} \
  --inputPath ${INPUT_PATH} \
  --outputPath ${OUTPUT_PATH} \
  --temp ${TEMPERATURE} \
  --topp ${TOP_P} \
  --topk ${TOP_K} \
  -t \
  $*

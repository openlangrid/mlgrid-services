#!/bin/bash

MODEL="${1:-decapoda-research/llama-7b-hf}"
shift
INSTRUCTION_PATH="${1:-./sample_instruction.txt}"
shift
INPUT_PATH="${1:-./sample.txt}"
shift
OUT_PATH_PREFIX="${1:-./sample}"
shift

docker-compose run --rm service python run.py \
  --instructionPath ${INSTRUCTION_PATH} \
  --utterancePath ${INPUT_PATH} \
  --outPathPrefix ${OUT_PATH_PREFIX} \
  --model ${MODEL} $*

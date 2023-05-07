#!/bin/bash

MODEL="${1:-decapoda-research/llama-7b-hf}"
shift
INPUT_PATH="${1:-./sample/input.txt}"
shift
INPUT_LANGUAGE="${1:-en}"
shift
OUTPUT_PATH="${1:-./sample/output.txt}"
shift

docker-compose run --rm service python instruct.py \
  --model ${MODEL} \
  --inputPath ${INPUT_PATH} \
  --inputLanguage ${INPUT_LANGUAGE} \
  --outputPath ${OUTPUT_PATH} \
  $*

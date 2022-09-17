#!/bin/bash

PROMPT="${1:-cute cat ear maid}"
shift
NUM_SAMPLES=${2:-3}
shift
OUT_PATH=${3:-temp}
shift

docker-compose run --rm service python run.py "$PROMPT" "$NUM_SAMPLES" "$OUT_PATH" "$@"

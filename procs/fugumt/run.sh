#!/bin/bash

MODEL="${1:-staka/fugumt-en-ja}"
shift
IN_PATH="${1:-./sample.txt}"
shift
OUT_PATH_PREFIX="${1:-./sample.txt}"
shift

docker-compose run --rm service python run.py $MODEL \
  --inPath "$IN_PATH" \
  --outPathPrefix "$OUT_PATH_PREFIX" \
  "$@"

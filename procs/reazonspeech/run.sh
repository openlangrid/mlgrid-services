#!/bin/bash

OUT_FILE_NAME=${1:-sample/test_ja_16k.wav}
shift

docker-compose run --rm service python run.py "$OUT_FILE_NAME" "$@"

#!/bin/bash

INPUT="${1:-test_ja_16k.wav}"
shift
LANGUAGE="${2:-Japanese}"
shift
MODEL="${3:-small}"
shift

docker-compose run --rm service whisper "$INPUT" --language "$LANGUAGE" --model "$MODEL" "$@"

#!/bin/bash

PROMPT="${1:-A fantasy landscape, trending on artstation}"
shift
INPUT_FILE=${2:-input.png}
shift
OUTPUT_PATH=${3:-temp/out}
shift

docker-compose run --rm service python run.py "$INPUT_FILE" "$PROMPT" 1 "$OUTPUT_PATH" "$@"

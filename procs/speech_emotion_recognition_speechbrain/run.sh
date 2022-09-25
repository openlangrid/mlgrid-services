#!/bin/bash

FILE="${1:-anger.wav}"
shift

docker-compose run --rm service python run.py "$FILE" "$@"

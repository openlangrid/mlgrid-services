#!/bin/bash

TEXT="${1:-My name is Wolfgang and I live in Berlin}"
shift

docker-compose run --rm service python run.py "$TEXT" "$@"

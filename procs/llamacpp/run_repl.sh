#!/bin/bash
MODEL=$1
shift

docker compose run -T --rm service python run_repl.py ./models/$MODEL $@

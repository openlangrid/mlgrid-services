#!/bin/bash
docker compose run -T --rm service python run_vllm_repl.py $@

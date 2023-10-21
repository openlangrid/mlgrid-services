#!/bin/bash
docker compose run -T --rm service python run_completion_peft_repl.py $@

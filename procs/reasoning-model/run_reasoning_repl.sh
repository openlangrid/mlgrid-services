#!/bin/bash
docker compose run -T --rm service /root/.local/bin/poetry run python run_reasoning_repl.py $@

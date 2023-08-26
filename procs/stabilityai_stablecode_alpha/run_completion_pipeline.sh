#!/bin/bash
docker compose run -T --rm service python3 run_completion_pipeline.py $@

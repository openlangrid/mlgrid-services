#!/bin/bash
. common.sh
docker compose run -T --rm service python run_pipeline.py $@

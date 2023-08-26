#!/bin/bash
docker compose run -T --rm service python run_chat_pipeline.py $@

#!/bin/bash
docker compose run -T --rm service python test_qwen.py $@

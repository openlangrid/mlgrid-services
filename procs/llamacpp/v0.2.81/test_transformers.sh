#!/bin/bash
docker compose run -T --rm service python test_transformers.py $@

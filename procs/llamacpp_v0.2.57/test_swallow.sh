#!/bin/bash
ORIG_MODEL_REPO=tokyotech-llm/Swallow-MX-8x7b-NVE-v0.1
GGUF_MODEL_REPO=mmnga/tokyotech-llm-Swallow-MX-8x7b-NVE-v0.1-gguf
GGUF_MODEL_PATTERN="*q8_0.gguf"
docker compose run -T --rm service python test.py $ORIG_MODEL_REPO $GGUF_MODEL_REPO $GGUF_MODEL_PATTERN


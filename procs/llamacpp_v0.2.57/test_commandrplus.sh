#!/bin/bash
ORIG_MODEL_REPO=CohereForAI/c4ai-command-r-plus
GGUF_MODEL_REPO=pmysl/c4ai-command-r-plus-GGUF
GGUF_MODEL_PATTERN="command-r-plus-Q2*"
docker compose run -T --rm service python test.py $ORIG_MODEL_REPO $GGUF_MODEL_REPO $GGUF_MODEL_PATTERN


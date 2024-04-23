#!/bin/bash
ORIG_MODEL_REPO=CohereForAI/c4ai-command-r-plus
GGUF_MODEL_REPO=pmysl/c4ai-command-r-plus-GGUF
GGUF_MODEL_PATTERN="command-r-plus-Q4_K_M-00001-of-00002.gguf"
docker compose run -T --rm service python test.py $ORIG_MODEL_REPO $GGUF_MODEL_REPO $GGUF_MODEL_PATTERN 48


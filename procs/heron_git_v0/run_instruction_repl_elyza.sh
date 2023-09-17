#!/bin/bash
docker compose run -T --rm service python run_instruction_repl_gitllama.py "turing-motors/heron-chat-git-ELYZA-fast-7b-v0" $@

#!/bin/bash

docker-compose run --rm service ./main -l ja -m model/ggml-large.bin \
  -f sample/test_ja_16k.wav -otxt

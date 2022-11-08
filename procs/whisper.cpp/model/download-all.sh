#!/bin/bash

models=( "tiny.en" "tiny" "base.en" "base" "small.en" "small" "medium.en" "medium" "large" )
for model in "${models[@]}"; do
    bash download-ggml-models.sh "$model"
done

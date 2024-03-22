#!/bin/bash

W=/workspace/Open-Sora
docker compose run --rm service \
  torchrun --standalone --nproc_per_node 1 ${W}/scripts/inference.py ${W}/configs/opensora/inference/16x256x256.py \
    --ckpt-path OpenSora-v1-HQ-16x256x256.pth --prompt-path ${W}/assets/texts/t2v_samples.txt

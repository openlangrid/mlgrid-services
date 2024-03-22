#!/bin/bash
MODEL=${1:-OpenSora-v1-HQ-16x256x256.pth}
shift

W=/workspace/Open-Sora
docker compose run --rm service \
  torchrun --standalone --nproc_per_node 1 sample.py ${W}/configs/opensora/inference/16x256x256.py \
    --ckpt-path ${MODEL} $@

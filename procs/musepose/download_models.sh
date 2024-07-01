#!/bin/bash

docker compose run --rm service \
  python download_models.py
docker compose run --rm service \
  wget https://download.openmmlab.com/mmdetection/v2.0/yolox/yolox_l_8x8_300e_coco/yolox_l_8x8_300e_coco_20211126_140236-d3bd2b23.pth \
  -O /workspace/MusePose/pretrained_weights/dwpose/yolox_l_8x8_300e_coco.pth

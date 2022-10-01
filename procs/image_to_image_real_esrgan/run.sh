#!/bin/bash

docker-compose run --rm service python3 inference_realesrgan.py \
  -i /work/samples/04.jpg \
  -o /work/samples --suffix out \
  -n RealESRGAN_x4plus -s 4 --face_enhance

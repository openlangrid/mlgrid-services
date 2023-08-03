#!/bin/bash

mkdir -p temp

docker-compose run --rm service python runSDXL.py \
  "1girl, aqua eyes, baseball cap, blonde hair, closed mouth, earrings, green background, hat, hoop earrings, jewelry, looking at viewer, shirt, short hair, simple background, solo, upper body, yellow shirt" \
  1 temp/out_sdxl_base \
  --modelPath "stabilityai/stable-diffusion-xl-base-1.0"

# "An astronaut riding a green horse"

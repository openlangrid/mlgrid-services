#!/bin/bash

mkdir -p temp

docker-compose run --rm service python runSDXLWithRefiner.py \
  "1girl, aqua eyes, baseball cap, blonde hair, closed mouth, earrings, green background, hat, hoop earrings, jewelry, looking at viewer, shirt, short hair, simple background, solo, upper body, yellow shirt" \
  1 temp/out_sdxl_base_with_refiner \
  --modelPath "stabilityai/stable-diffusion-xl-base-1.0" \
  --useRefiner

# "An astronaut riding a green horse"

#!/bin/bash

mkdir -p temp

docker-compose run --rm service python run.py \
  "1girl, aqua eyes, baseball cap, blonde hair, closed mouth, earrings, green background, hat, hoop earrings, jewelry, looking at viewer, shirt, short hair, simple background, solo, upper body, yellow shirt" \
  1 temp/out_sd15 --modelPath "runwayml/stable-diffusion-v1-5"

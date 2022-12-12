#!/bin/bash

docker-compose run --rm service python3 run.py "1girl, aqua eyes, baseball cap, blonde hair, closed mouth, earrings, green background, hat, hoop earrings, jewelry, looking at viewer, shirt, short hair, simple background, solo, upper body, yellow shirt" \
  1 temp/out_sd21 --modelPath "stabilityai/stable-diffusion-2-1"

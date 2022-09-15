#!/bin/bash

docker-compose run --rm service python inference_codeformer.py --test_path /work/inputs/04 --w 0.7 --bg_upsampler realesrgan --face_upsample

#!/bin/bash

max=20
for ((i=0; i < $max; i++)); do
docker-compose run --rm \
  service \
  python ./example/python/run.py \
     --text "こんにちは" \
     --speaker_id $i \
     --f0_speaker_id 0 \
     --f0_correct 0 \
     --root_dir_path="./release" \
     --use_gpu \
     --out_file_name="/work/sample/${i}_out.wav" "$@"
done

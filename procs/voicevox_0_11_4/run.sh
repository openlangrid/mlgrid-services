#!/bin/bash

OUT_FILE_NAME=${1:-/work/sample/out.wav}
shift
SPEAKER_ID=${1:-3}
shift

docker-compose run --rm \
  service \
  python ./example/python/run.py \
     --text "こんにちは" \
     --speaker_id ${SPEAKER_ID} \
     --f0_speaker_id 0 \
     --f0_correct 0 \
     --root_dir_path="./release" \
     --use_gpu \
     --out_file_name="${OUT_FILE_NAME}" "$@"

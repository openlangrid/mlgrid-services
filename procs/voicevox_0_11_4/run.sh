#!/bin/bash

OUT_FILE_NAME=${1:-/work/sample/out.wav}
shift
SPEAKER_ID=${1:-3}
shift

docker compose run --rm \
  service \
  python ./run.py --dict-dir "./open_jtalk_dic_utf_8-1.11" \
    --text "こんにちは" --out /work/sample/out.wav --speaker-id  1

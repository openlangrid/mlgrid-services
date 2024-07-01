#!/bin/bash

DIR=${1:-samples/01_demo}

docker compose run --rm service \
  bash -c "cd /workspace/MusePose && python pose_align.py --imgfn_refer /work/$DIR/ref.png --vidfn /work/$DIR/dance.mp4 && cp ./assets/poses/align/img_ref_video_dance.mp4 /work/$DIR/"
docker compose run --rm service \
  bash -c "cd /workspace/MusePose && python test_stage_2.py --config /work/$DIR/test_stage_2.yaml && cp -Rf output/* /work/$DIR/"

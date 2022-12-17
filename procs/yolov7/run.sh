#!/bin/bash


JPG=${1:-./sample/zidane.jpg}
shift
WEIGHT=${1:-yolov7-e6e.pt}

docker-compose run --rm \
  -v $(pwd)/detect.py:/workspace/yolov7/detect.py \
  -v $(pwd)/$JPG:/workspace/yolov7/$JPG \
  -v $(pwd)/${JPG}_result:/workspace/yolov7/runs/detect/prj \
  service \
  python detect.py --source $JPG --weights /work/weights/$WEIGHT \
    --conf 0.25 --img-size 1280 --device 0 --save-txt --nosave \
    --name prj --no-trace --save-conf

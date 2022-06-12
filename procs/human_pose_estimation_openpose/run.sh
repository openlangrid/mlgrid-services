#!/bin/bash

JPG=${1:-zidane.jpg}
MODEL=${2:-hog}

docker-compose run --rm facerecognition python3 run.py ${JPG} ${MODEL}

#!/bin/bash

JPG=${1:-zidane.jpg}
MODEL=${2:-hog}

docker-compose run --rm openpose python3 run.py ${JPG} ${MODEL}

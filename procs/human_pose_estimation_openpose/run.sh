#!/bin/bash

JPG=${1:-IP210215TAN000004000.jpeg}
MODEL=${2:-hog}

docker-compose run --rm openpose python3 run.py ${JPG} ${MODEL}

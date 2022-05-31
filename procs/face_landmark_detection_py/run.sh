#!/bin/bash

JPG=${1:-zidane.jpg}

docker-compose run --rm facerecognition python3 run.py ${JPG}

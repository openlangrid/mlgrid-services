#!/bin/bash

MODEL=${1:-VGG19}
JPG=${2:-cat.jpg}

docker-compose run --rm keras-gpu python run${MODEL}.py ${JPG}


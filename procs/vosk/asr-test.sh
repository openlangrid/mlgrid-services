#!/bin/bash

FILE=${1:-test_ja_2_16k.wav}

IPS=$(hostname -I)
ARR=($IPS)
HOST=${ARR[0]}

docker run --rm --name vosk-test \
   -v $(pwd):/work \
   -w /work alphacep/kaldi-vosk-server:latest \
     python3 asr-test.py ws://$HOST:2700 $FILE

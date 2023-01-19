import warnings
warnings.filterwarnings('ignore')


import os

TOKEN = os.environ['HUGGINGFACE_TOKEN']

from huggingface_hub import login
login(TOKEN)


import argparse, os
parser = argparse.ArgumentParser()
parser.add_argument("inputFilePath",type=str, default="temp/test_ja_16k.wav")
args = parser.parse_args()
inputFilePath = args.inputFilePath
outputFilePath = f"{inputFilePath}.out.txt"


import torch
from espnet2.bin.asr_inference import Speech2Text

device = "cuda" if torch.cuda.is_available() else "cpu"
beam_size = 5#@param {type:"integer"}

#from pathlib import Path

reazonspeech = Speech2Text.from_pretrained(
  #"/opt/conda/lib/python3.7/site-packages/espnet_model_zoo/models--reazon-research--reazonspeech-espnet-v1/snapshots/42f0366852e9ff23af808ccff368f0bfb9038809/",
  "reazon-research/reazonspeech-espnet-v1",
  beam_size=beam_size,
  batch_size=0,
  device=device
)

import librosa

speech, sample_rate = librosa.load(inputFilePath)

with open(outputFilePath, "w") as f:
  f.write(reazonspeech(speech)[0][0])

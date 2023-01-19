import warnings
warnings.filterwarnings('ignore')

from huggingface_hub import notebook_login
notebook_login()

import torch
from espnet2.bin.asr_inference import Speech2Text

device = "cuda" if torch.cuda.is_available() else "cpu"
beam_size = 5#@param {type:"integer"}

reazonspeech = Speech2Text.from_pretrained(
  "reazon-research/reazonspeech-espnet-v1",
  beam_size=beam_size,
  batch_size=0,
  device=device
)

import librosa
import librosa.display
from IPython.display import display, Audio
import matplotlib.pyplot as plt
from datetime import datetime
def display_speech(speech, sample_rate):
  display(Audio(speech, rate=sample_rate))
  librosa.display.waveshow(speech, sr=sample_rate)
  plt.show()

def transcribe(speech):
  asr_results = reazonspeech(speech)
  print(asr_results[0][0])


speech, sample_rate = librosa.load(file_name, sr=sample_rate)

print(transcribe(speech))

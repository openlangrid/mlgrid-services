FROM pytorch/pytorch:1.12.1-cuda11.3-cudnn8-devel

RUN apt update && \
  apt install -y software-properties-common git curl 
ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone


RUN apt install -y wget unzip cmake 
RUN git clone https://github.com/VOICEVOX/voicevox_core -b 0.11.4 && \
  cd voicevox_core && \
  echo 1 | python configure.py --use_gpu --voicevox_version 0.11.4 --ort_version v1.10.0 && \
  echo 0.11.4 > VERSION.txt && \
  pip install -r requirements.txt && \
  pip install . && \
  pip install pyopenjtalk
  
RUN apt install -y libsndfile1-dev && \
  pip install soundfile

# 一度実行してモデルをダウンロードしておく
RUN cd voicevox_core && \
  python ./example/python/run.py \
     --text "こんにちは" \
     --speaker_id 3 \
     --f0_speaker_id 0 \
     --f0_correct 0 \
     --root_dir_path="./release" \
     --use_gpu

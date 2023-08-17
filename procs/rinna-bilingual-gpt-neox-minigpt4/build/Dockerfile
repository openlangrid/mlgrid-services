FROM pytorch/pytorch:2.0.1-cuda11.7-cudnn8-devel

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && \
  apt install -y software-properties-common git curl libgl1


RUN pip install transformers accelerate
RUN pip install sentencepiece

RUN mkdir /work && \
  cd /work && \
  git clone https://github.com/Vision-CAIR/MiniGPT-4.git && \
  cd ./MiniGPT-4 && \
  git checkout 22d8888

COPY minigpt4-requirements.txt ./
RUN pip install -r minigpt4-requirements.txt

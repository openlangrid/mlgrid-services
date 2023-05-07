FROM pytorch/pytorch:2.0.0-cuda11.7-cudnn8-devel

RUN apt update && \
  apt install -y software-properties-common git curl 
ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN git clone https://github.com/Blealtan/RWKV-LM-LoRA && \
  cd RWKV-LM-LoRA/RWKV-v4neo && \
  pip install transformers

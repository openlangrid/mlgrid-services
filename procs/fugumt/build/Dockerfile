FROM pytorch/pytorch:1.12.1-cuda11.3-cudnn8-devel

RUN apt update && \
  apt install -y software-properties-common git curl 
ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN pip install transformers sentencepiece sacremoses

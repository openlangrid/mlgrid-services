FROM pytorch/pytorch:2.0.0-cuda11.7-cudnn8-devel

RUN apt update && \
  apt install -y software-properties-common git curl 
ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN pip install transformers accelerate
RUN pip install sentencepiece

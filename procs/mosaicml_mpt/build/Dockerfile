FROM mosaicml/pytorch:1.13.1_cu117-python3.10-ubuntu20.04

RUN apt update && \
  apt install -y software-properties-common git curl 
ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN pip install transformers einops

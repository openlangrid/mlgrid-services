FROM pytorch/pytorch:2.0.1-cuda11.7-cudnn8-devel

RUN apt update && \
  DEBIAN_FRONTEND=noninteractive apt install -y software-properties-common git curl 
ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN pip install ffmpeg demucs

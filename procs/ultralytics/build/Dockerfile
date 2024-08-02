FROM pytorch/pytorch:2.2.0-cuda12.1-cudnn8-devel

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && \
  apt install -y software-properties-common git curl libgl1

RUN pip install opencv-python-headless opencv-contrib-python
#RUN pip install opencv-python-headless opencv-contrib-python

RUN pip install ultralytics

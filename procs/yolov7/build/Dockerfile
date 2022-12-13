FROM pytorch/pytorch:1.12.1-cuda11.3-cudnn8-devel

RUN apt update && \
  apt install -y software-properties-common git curl 
ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone


RUN apt install -y git libgl1-mesa-dev libglib2.0-0
RUN git clone https://github.com/WongKinYiu/yolov7 && \
  cd yolov7 && \
  pip install -r requirements.txt

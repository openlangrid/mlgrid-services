#FROM tensorflow/tensorflow:2.8.0-gpu
FROM pytorch/pytorch:1.11.0-cuda11.3-cudnn8-devel

ADD https://developer.download.nvidia.com/compute/cuda/repos/ubuntu2004/x86_64/cuda-keyring_1.0-1_all.deb .
RUN apt-key del 7fa2af80 && \
  dpkg -i cuda-keyring_1.0-1_all.deb && \
  rm /etc/apt/sources.list.d/cuda.list && \
  rm /etc/apt/sources.list.d/nvidia-ml.list && \
  apt update

RUN apt install -y git libgl1-mesa-dev libglib2.0-0
RUN git clone https://github.com/ultralytics/yolov5 && \
  cd yolov5 && \
  git checkout aa7a0e91b175a9b2c64c3eca44dcafe77dc23d03 && \
  pip install -r requirements.txt

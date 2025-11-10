FROM pytorch/pytorch:2.8.0-cuda12.6-cudnn9-devel

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && \
  apt install -y software-properties-common git curl libgl1 libgomp1

RUN pip install -U transformers==4.55.0 kernels accelerate

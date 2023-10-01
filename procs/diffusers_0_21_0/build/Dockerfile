FROM pytorch/pytorch:2.0.1-cuda11.7-cudnn8-devel

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && \
  apt install -y software-properties-common git curl libgl1


RUN pip install invisible_watermark transformers accelerate \
  safetensors ftfy
RUN pip install diffusers[torch]==0.21.0 controlnet_aux==0.0.7

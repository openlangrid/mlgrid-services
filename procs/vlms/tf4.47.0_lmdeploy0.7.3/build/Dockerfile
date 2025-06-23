FROM pytorch/pytorch:2.4.0-cuda12.1-cudnn9-devel

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && \
  apt install -y software-properties-common git curl libgl1 libgomp1


RUN pip install -U transformers==4.47.0 torch torchvision pillow protobuf sentencepiece accelerate
RUN pip install lmdeploy>=0.7.3
RUN pip install timm 

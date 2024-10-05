FROM pytorch/pytorch:2.4.0-cuda12.1-cudnn9-devel

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && \
  apt install -y software-properties-common git curl libgl1


RUN pip install invisible_watermark accelerate \
  safetensors ftfy scipy
RUN pip install transformers==4.45.1 protobuf==5.28.2 sentencepiece
RUN pip install diffusers[torch]==0.30.3 controlnet_aux==0.0.9

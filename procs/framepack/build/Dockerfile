FROM pytorch/pytorch:2.6.0-cuda12.6-cudnn9-devel

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && \
  apt install -y software-properties-common git curl libgl1


RUN git clone https://github.com/lllyasviel/FramePack && \
  cd FramePack && \
  pip install -r requirements.txt

RUN pip install flash-attn xformers

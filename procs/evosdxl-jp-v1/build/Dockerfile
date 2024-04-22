FROM pytorch/pytorch:2.0.1-cuda11.7-cudnn8-devel

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && \
  apt install -y software-properties-common git curl libgl1


RUN git clone https://huggingface.co/SakanaAI/EvoSDXL-JP-v1 && \
  cd EvoSDXL-JP-v1 && \
  pip install -r requirements.txt

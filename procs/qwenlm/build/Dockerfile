FROM pytorch/pytorch:2.0.1-cuda11.7-cudnn8-devel

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && \
  apt install -y software-properties-common git curl libgl1


RUN pip install transformers accelerate
RUN pip install sentencepiece


RUN git clone https://github.com/QwenLM/Qwen-7B && \
  cd Qwen-7B && \
  pip install -r requirements.txt && \
  cd .. && \
  git clone -b v1.0.8 https://github.com/Dao-AILab/flash-attention && \
  cd flash-attention && \
  pip install . && \
  pip install csrc/layer_norm csrc/rotary && \
  cd ..


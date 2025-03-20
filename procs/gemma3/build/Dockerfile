FROM pytorch/pytorch:2.5.1-cuda12.4-cudnn9-devel

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && \
  apt install -y software-properties-common git curl libgl1 libgomp1

RUN pip install accelerate bitsandbytes einops scipy sentencepiece protobuf flash_attn 
RUN pip install git+https://github.com/huggingface/transformers@v4.49.0-Gemma-3

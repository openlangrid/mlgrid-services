FROM pytorch/pytorch:2.0.1-cuda11.7-cudnn8-devel

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && \
  apt install -y software-properties-common git curl libgl1


RUN pip install transformers accelerate
RUN pip install sentencepiece
RUN pip install bitsandbytes einops scipy

RUN git clone https://github.com/turingmotors/heron.git && \
  cd heron && \
  pip install --upgrade pip && \
  pip install -e .


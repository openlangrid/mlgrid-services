FROM pytorch/pytorch:2.3.0-cuda11.8-cudnn8-devel

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && \
  apt install -y software-properties-common git curl libgl1


RUN git clone https://github.com/KwaiVGI/LivePortrait && \
  cd LivePortrait && \
  pip install -r requirements.txt

FROM pytorch/pytorch:2.0.1-cuda11.7-cudnn8-devel

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && \
  apt install -y software-properties-common git curl libgl1

RUN git clone https://github.com/Plachtaa/VALL-E-X.git && \
  cd VALL-E-X && \
  pip install -r requirements.txt

RUN pip install scipy

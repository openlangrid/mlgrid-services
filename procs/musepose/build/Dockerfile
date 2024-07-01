FROM pytorch/pytorch:2.0.1-cuda11.7-cudnn8-devel

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && \
  apt install -y software-properties-common git curl libgl1

RUN pip install --no-cache-dir -U openmim && \
  mim install mmengine && \
  mim install "mmcv>=2.0.1" && \
  mim install "mmdet>=3.1.0" && \
  mim install "mmpose>=1.1.0"

RUN git clone https://github.com/TMElyralab/MusePose && \
  cd MusePose && \
  pip install -r requirements.txt

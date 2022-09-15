FROM pytorch/pytorch:1.11.0-cuda11.3-cudnn8-devel

ADD https://developer.download.nvidia.com/compute/cuda/repos/ubuntu2004/x86_64/cuda-keyring_1.0-1_all.deb .
RUN apt-key del 7fa2af80 && \
  dpkg -i cuda-keyring_1.0-1_all.deb && \
  rm /etc/apt/sources.list.d/cuda.list && \
  rm /etc/apt/sources.list.d/nvidia-ml.list && \
  apt update

RUN apt update && apt install -y git

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN apt update && apt install -y libgl1-mesa-dev libopencv-dev --fix-missing

RUN git clone https://github.com/sczhou/CodeFormer.git && \
  cd CodeFormer && \
  pip install -r requirements.txt && \
  python basicsr/setup.py develop && \
  python scripts/download_pretrained_models.py facelib && \
  python scripts/download_pretrained_models.py CodeForme
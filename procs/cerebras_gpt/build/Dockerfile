FROM pytorch/pytorch:1.10.0-cuda11.3-cudnn8-devel

ADD https://developer.download.nvidia.com/compute/cuda/repos/ubuntu2004/x86_64/cuda-keyring_1.0-1_all.deb .
RUN apt-key del 7fa2af80 && \
  dpkg -i cuda-keyring_1.0-1_all.deb && \
  rm /etc/apt/sources.list.d/cuda.list && \
  rm /etc/apt/sources.list.d/nvidia-ml.list && \
  apt update

RUN apt update && \
  apt install -y software-properties-common git curl 
ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone


RUN git clone https://github.com/Cerebras/modelzoo.git && \
  cd modelzoo && \
  pip install -r requirements_pytorch_gpu.txt

RUN pip install torch==1.11.0+cu113 torchvision==0.12.0+cu113 torchaudio==0.11.0 --extra-index-url https://download.pytorch.org/whl/cu113

FROM pytorch/pytorch:1.11.0-cuda11.3-cudnn8-devel

ADD https://developer.download.nvidia.com/compute/cuda/repos/ubuntu2004/x86_64/cuda-keyring_1.0-1_all.deb .
RUN apt-key del 7fa2af80 && \
  dpkg -i cuda-keyring_1.0-1_all.deb && \
  rm /etc/apt/sources.list.d/cuda.list && \
  rm /etc/apt/sources.list.d/nvidia-ml.list && \
  apt update

RUN apt update && apt install -y git

RUN pip install ftfy regex tqdm transformers==4.15.0 timm==0.4.12 fairscale==0.4.4
RUN pip install git+https://github.com/openai/CLIP.git
RUN git clone https://github.com/pharmapsychotic/clip-interrogator.git
RUN git clone https://github.com/salesforce/BLIP

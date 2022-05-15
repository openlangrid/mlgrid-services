FROM nvidia/cuda:11.4.0-cudnn8-devel-ubuntu20.04
#tensorflow/tensorflow:2.8.0-gpu

ADD https://developer.download.nvidia.com/compute/cuda/repos/ubuntu2004/x86_64/cuda-keyring_1.0-1_all.deb .
RUN apt-key del 7fa2af80 && \
  dpkg -i cuda-keyring_1.0-1_all.deb && \
  rm -Rf /etc/apt/sources.list.d/cuda.list && \
  rm -Rf /etc/apt/sources.list.d/nvidia-ml.list && \
  apt update

RUN apt install -y git python3 python3-pip

RUN pip install typing-extensions==4.1.1 && \
    pip install --upgrade "jax[cuda]" -f https://storage.googleapis.com/jax-releases/jax_releases.html

RUN pip install -q git+https://github.com/huggingface/transformers.git && \
    pip install -q git+https://github.com/patil-suraj/vqgan-jax.git && \
    pip install -q git+https://github.com/borisdayma/dalle-mini.git

FROM nvidia/cuda:11.8.0-cudnn8-devel-ubuntu20.04

RUN apt update && apt install -y git python3 python3-pip

RUN pip install --upgrade pip && \
  pip install --upgrade "jaxlib==0.3.22+cuda11.cudnn82" -f https://storage.googleapis.com/jax-releases/jax_cuda_releases.html && \
  pip install -q dalle-mini && \
  pip install -q git+https://github.com/patil-suraj/vqgan-jax.git

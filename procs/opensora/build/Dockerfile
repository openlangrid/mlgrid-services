FROM pytorch/pytorch:2.2.0-cuda12.1-cudnn8-devel

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && \
  apt install -y software-properties-common git curl libgl1

#pip3 install torch torchvision

# install flash attention (optional)
RUN pip install packaging ninja
RUN pip install flash-attn --no-build-isolation
# install apex (optional)
RUN pip install -v --disable-pip-version-check --no-cache-dir --no-build-isolation --config-settings "--build-option=--cpp_ext" --config-settings "--build-option=--cuda_ext" git+https://github.com/NVIDIA/apex.git
# install xformers
RUN pip3 install -U xformers --index-url https://download.pytorch.org/whl/cu121

# install this project
RUN git clone https://github.com/hpcaitech/Open-Sora && \
  cd Open-Sora && \
  pip install -v .

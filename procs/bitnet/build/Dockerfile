FROM pytorch/pytorch:2.4.0-cuda12.1-cudnn9-devel

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && \
  apt install -y software-properties-common git curl libgl1 libgomp1

RUN pip install accelerate bitsandbytes einops scipy sentencepiece protobuf flash_attn 
#RUN CMAKE_ARGS="-DLLAMA_CUDA=on" FORCE_CMAKE=1 pip install llama-cpp-python==0.2.81
RUN pip install git+https://github.com/huggingface/transformers.git@096f25ae1f501a084d8ff2dcaf25fbc2bd60eba4


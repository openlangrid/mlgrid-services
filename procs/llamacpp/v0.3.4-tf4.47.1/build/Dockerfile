FROM pytorch/pytorch:2.4.0-cuda12.1-cudnn9-devel

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && \
  apt install -y software-properties-common git curl libgl1 libgomp1

RUN pip install accelerate bitsandbytes einops scipy sentencepiece protobuf flash_attn 
RUN pip install transformers==4.47.1
#RUN CMAKE_ARGS="-DLLAMA_CUDA=on" FORCE_CMAKE=1 pip install llama-cpp-python==0.2.81
RUN pip install llama-cpp-python==0.3.4 \
  --extra-index-url https://abetlen.github.io/llama-cpp-python/whl/cu121

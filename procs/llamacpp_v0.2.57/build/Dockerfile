FROM pytorch/pytorch:2.2.0-cuda12.1-cudnn8-devel

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && \
  apt install -y software-properties-common git curl libgl1


RUN pip install accelerate bitsandbytes einops scipy sentencepiece protobuf
RUN pip install transformers==4.39.3
#RUN CMAKE_ARGS="-DLLAMA_CUBLAS=on" pip install llama-cpp-python==0.2.62
RUN pip install llama-cpp-python==0.2.62 \
  --extra-index-url https://abetlen.github.io/llama-cpp-python/whl/cu121

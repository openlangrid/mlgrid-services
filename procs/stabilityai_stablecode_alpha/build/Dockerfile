FROM huggingface/transformers-pytorch-gpu:4.29.2

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && \
  apt install -y software-properties-common git curl libgl1

RUN pip install accelerate xformer sentencepiece bitsandbytes einops scipy

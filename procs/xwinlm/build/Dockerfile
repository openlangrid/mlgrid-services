FROM pytorch/pytorch:2.0.1-cuda11.7-cudnn8-devel

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && \
  apt install -y software-properties-common git curl libgl1


RUN pip install transformers accelerate
RUN pip install sentencepiece
RUN pip install bitsandbytes einops scipy
RUN pip install transformers>=4.32.0 optimum>=1.12.0
RUN pip install auto-gptq --extra-index-url https://huggingface.github.io/autogptq-index/whl/cu117/

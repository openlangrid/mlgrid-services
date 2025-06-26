FROM diffusers/diffusers-pytorch-cuda:latest

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN pip install safetensors ftfy controlnet_aux==0.0.10
RUN pip install imageio-ffmpeg
RUN pip install diffusers==0.33.1 xformers
RUN pip install sentencepiece


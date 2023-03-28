FROM pytorch/pytorch:2.0.0-cuda11.7-cudnn8-devel

RUN apt update && \
  apt install -y software-properties-common git curl 
ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# RUN add-apt-repository ppa:deadsnakes/ppa && \
#   apt update && \
#   apt install -y python3.10-distutils python3.10-dev
# RUN curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py && \
#   python3.10 get-pip.py --user
# 
# RUN python3.10 -m pip install diffusers[torch]==0.14.0 accelerate transformers ftfy scipy invisible-watermark
# 
# RUN rm /opt/conda/bin/python3 && \
#   ln -s /usr/bin/python3.10 /opt/conda/bin/python3

RUN pip install diffusers[torch]==0.14.0 accelerate transformers
RUN pip install ftfy scipy invisible-watermark

FROM pytorch/pytorch:1.12.1-cuda11.3-cudnn8-devel

RUN apt update && \
  apt install -y software-properties-common git curl 
ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN add-apt-repository ppa:deadsnakes/ppa && \
  apt update && \
  apt install -y python3.9-distutils python3.9-dev
RUN curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py && \
  python3.9 get-pip.py --user

RUN python3.9 -m pip install diffusers[torch]==0.8.1 transformers ftfy accelerate scipy invisible-watermark

RUN python3.9 -m pip install ninja
# RUN python3.9 -m pip install -v -U git+https://github.com/facebookresearch/xformers.git@main#egg=xformers
RUN git clone https://github.com/facebookresearch/xformers.git && \
  cd xformers && \
  git submodule update --init --recursive && \
  python3.9 -m pip install -r requirements.txt && \
  python3.9 -m pip install -e . 

RUN rm /opt/conda/bin/python3 && \
  ln -s /usr/bin/python3.9 /opt/conda/bin/python3

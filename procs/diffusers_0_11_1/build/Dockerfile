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

RUN python3.9 -m pip install --upgrade git+https://github.com/huggingface/transformers/
RUN python3.9 -m pip install diffusers[torch]==0.11.1 ftfy accelerate scipy invisible-watermark

#ADD ./xformers-0.0.15.dev0+a1876fc.d20221128-cp37-cp37m-linux_x86_64.whl ./
#RUN python3.9 -m pip install -U ./xformers-0.0.15.dev0+a1876fc.d20221128-cp37-cp37m-linux_x86_64.whl
RUN python3.9 -m pip install ninja
RUN git clone https://github.com/facebookresearch/xformers.git && \
  cd xformers && \
  git submodule update --init --recursive && \
  python3.9 -m pip install -r requirements.txt && \
  python3.9 -m pip install -e . 

RUN rm /opt/conda/bin/python3 && \
  ln -s /usr/bin/python3.9 /opt/conda/bin/python3

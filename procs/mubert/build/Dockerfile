FROM pytorch/pytorch:1.12.1-cuda11.3-cudnn8-devel

RUN pip install torchvision==0.13.1+cu113 --extra-index-url https://download.pytorch.org/whl/cu113
RUN pip install -U sentence-transformers
RUN pip install httpx
ADD setup.py setup.py
RUN python3 setup.py

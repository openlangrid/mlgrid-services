FROM pytorch/pytorch:2.0.1-cuda11.7-cudnn8-devel

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && \
  apt install -y software-properties-common git curl libgl1

RUN git clone -b dev https://github.com/camenduru/IDM-VTON-hf
RUN pip install -r IDM-VTON-hf/requirements.txt
RUN apt update && apt install -y aria2
RUN aria2c --console-log-level=error -c -x 16 -s 16 -k 1M https://huggingface.co/camenduru/IDM-VTON/resolve/main/densepose/model_final_162be9.pkl -d /content/IDM-VTON-hf/ckpt/densepose -o model_final_162be9.pkl && \
  aria2c --console-log-level=error -c -x 16 -s 16 -k 1M https://huggingface.co/camenduru/IDM-VTON/resolve/main/humanparsing/parsing_atr.onnx -d /content/IDM-VTON-hf/ckpt/humanparsing -o parsing_atr.onnx && \
  aria2c --console-log-level=error -c -x 16 -s 16 -k 1M https://huggingface.co/camenduru/IDM-VTON/resolve/main/humanparsing/parsing_lip.onnx -d /content/IDM-VTON-hf/ckpt/humanparsing -o parsing_lip.onnx && \
  aria2c --console-log-level=error -c -x 16 -s 16 -k 1M https://huggingface.co/camenduru/IDM-VTON/resolve/main/openpose/ckpts/body_pose_model.pth -d /content/IDM-VTON-hf/ckpt/openpose/ckpts -o body_pose_model.pth

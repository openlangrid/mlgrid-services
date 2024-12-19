FROM pytorch/pytorch:2.2.0-cuda12.1-cudnn8-devel

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt update && \
  apt install -y software-properties-common git curl libgl1


RUN apt update && apt install -y pipx && pipx ensurepath && pipx install poetry
RUN git clone https://github.com/Hajime-Y/reasoning-model.git && \
  cd reasoning-model && \
  /root/.local/bin/poetry install

RUN cd reasoning-model && /root/.local/bin/poetry add accelerate==0.26.0 sentencepiece

from ubuntu:20.04

RUN apt update && \
  apt install -y build-essential git && \
  apt clean
ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN git clone https://github.com/ggerganov/whisper.cpp && \
  cd whisper.cpp && \
  make

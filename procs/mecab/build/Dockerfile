FROM python:3.12

# mecabの導入
RUN apt -y update && \
  apt install -y mecab libmecab-dev mecab-ipadic-utf8 && \
  apt install -y git make curl xz-utils file sudo

# mecab-ipadic-NEologdのインストール
RUN git clone --depth 1 https://github.com/neologd/mecab-ipadic-neologd.git && \
  cd mecab-ipadic-neologd && \
  ./bin/install-mecab-ipadic-neologd -n -y && \
  echo dicdir = `mecab-config --dicdir`"/mecab-ipadic-neologd">/etc/mecabrc && \
  cp /etc/mecabrc /usr/local/etc && \
  cd ..

RUN pip3 install mecab-python3

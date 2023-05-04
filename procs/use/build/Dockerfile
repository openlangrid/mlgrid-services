FROM tensorflow/tensorflow:2.12.0-gpu

ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN pip install tensorflow-text

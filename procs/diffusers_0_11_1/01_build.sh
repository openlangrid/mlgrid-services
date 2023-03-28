#!/bin/bash

cp ../libs/xformers-0.0.15.dev0+a1876fc.d20221128-cp37-cp37m-linux_x86_64.whl ./build/
docker-compose build

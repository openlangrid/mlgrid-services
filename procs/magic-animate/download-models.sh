#!/bin/bash
git clone -b fp16 https://huggingface.co/runwayml/stable-diffusion-v1-5 ./cache/stable-diffusion-v1-5/
git clone https://huggingface.co/stabilityai/sd-vae-ft-mse ./cache/sd-vae-ft-mse/
git lfs clone https://huggingface.co/zcxu-eric/MagicAnimate ./cache/MagicAnimate/

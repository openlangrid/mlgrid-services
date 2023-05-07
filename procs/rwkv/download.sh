#!/bin/bash

BASE=RWKV-4-Pile-14B-Instruct-test5-20230329-ctx4096.pth
LORA=rwkv-500-2305010842.pth

mkdir -p ../cache/rwkv

wget -O ../cache/rwkv/$BASE https://huggingface.co/BlinkDL/rwkv-4-pile-14b/resolve/main/$BASE
wget -O ../cache/rwkv/$LORA https://huggingface.co/shi3z/RWKV-LM-LoRA-Alpaca-Cleaned-Japan/resolve/main/$LORA

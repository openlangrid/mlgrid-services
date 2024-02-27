#!/bin/bash
set -eu

declare -A models=(
  ["karakuri-lm-70b-chat-v0.1-gguf"]="
    karakuri-lm-70b-chat-v0.1-q5_K_S.gguf
  "
  ["ELYZA-japanese-Llama-2-13b-fast-instruct-gguf"]="
    ELYZA-japanese-Llama-2-13b-fast-instruct-q5_K_M.gguf
    ELYZA-japanese-Llama-2-13b-fast-instruct-q5_K_S.gguf
  "
  ["ELYZA-japanese-Llama-2-7b-fast-instruct-gguf"]="
    ELYZA-japanese-Llama-2-7b-fast-instruct-q5_K_M.gguf
    ELYZA-japanese-Llama-2-7b-fast-instruct-q5_K_S.gguf
  "
  ["cyberagent-calm2-7b-chat-dpo-experimental-gguf"]="
    cyberagent-calm2-7b-chat-dpo-experimental-q5_K_S.gguf
  "
  ["rinna-nekomata-14b-instruction-gguf"]="
    rinna-nekomata-14b-instruction-q5_K_M.gguf
    rinna-nekomata-14b-instruction-q5_K_S.gguf
  "
  ["rinna-nekomata-7b-instruction-gguf"]="
    rinna-nekomata-7b-instruction-q5_K_M.gguf
    rinna-nekomata-7b-instruction-q5_K_S.gguf
  "
  ["rinna-youri-7b-instruction-gguf"]="
    rinna-youri-7b-instruction-q5_K_M.gguf
    rinna-youri-7b-instruction-q5_K_S.gguf
  "
  ["rinna-youri-7b-chat-gguf"]="
    rinna-youri-7b-chat-q5_K_M.gguf
    rinna-youri-7b-chat-q5_K_S.gguf
  "
  ["llm-jp-13b-instruct-dolly-en-ja-oasst-v1.1-gguf"]="
    llm-jp-13b-instruct-dolly-en-ja-oasst-v1.1-q5_K_M.gguf
    llm-jp-13b-instruct-dolly-en-ja-oasst-v1.1-q5_K_S.gguf
  "
)

for D in "${!models[@]}"; do
  MS=(`echo ${models[${D}]}`)
  for M in "${MS[@]}"; do
    if [ ! -e ./models/${M} ]; then
      wget https://huggingface.co/mmnga/${D}/resolve/main/${M}?download=true \
        -O ./models/${M}
    fi
  done
done


declare -A qwenmodelparts=(
  ["Qwen1.5-14B-Chat-GGUF"]="
    qwen1_5-14b-chat-q5_k_m.gguf
  "
  ["Qwen1.5-72B-Chat-GGUF"]="
    qwen1_5-72b-chat-q5_k_m.gguf.a
    qwen1_5-72b-chat-q5_k_m.gguf.b
    qwen1_5-72b-chat-q4_k_m.gguf.a
    qwen1_5-72b-chat-q4_k_m.gguf.b
  "
)

for D in "${!qwenmodelparts[@]}"; do
  MS=(`echo ${qwenmodelparts[${D}]}`)
  for M in "${MS[@]}"; do
    if [ ! -e ./models/${M} ]; then
      wget https://huggingface.co/Qwen/${D}/resolve/main/${M}?download=true \
        -O ./models/${M}
    fi
  done
done

qwenmodels=(qwen1_5-72b-chat-q4_k_m.gguf qwen1_5-72b-chat-q5_k_m.gguf)
for M in "${qwenmodels[@]}"; do
  if [ ! -e ./models/${M} ]; then
    cat ./models/${M}.* > ./models/${M}
  fi
done
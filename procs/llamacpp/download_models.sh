
if [ ! -e ./models/karakuri-lm-70b-chat-v0.1-q5_K_S.gguf ]; then
  wget https://huggingface.co/mmnga/karakuri-lm-70b-chat-v0.1-gguf/resolve/main/karakuri-lm-70b-chat-v0.1-q5_K_S.gguf?download=true \
    -O ./models/karakuri-lm-70b-chat-v0.1-q5_K_S.gguf
fi

if [ ! -e ./models/ELYZA-japanese-Llama-2-13b-fast-instruct-q5_K_S.gguf ]; then
  wget https://huggingface.co/mmnga/ELYZA-japanese-Llama-2-13b-fast-instruct-gguf/resolve/main/ELYZA-japanese-Llama-2-13b-fast-instruct-q5_K_S.gguf?download=true \
    -O ./models/ELYZA-japanese-Llama-2-13b-fast-instruct-q5_K_S.gguf
fi

if [ ! -e ./models/cyberagent-calm2-7b-chat-dpo-experimental-q5_K_S.gguf ]; then
  wget https://huggingface.co/mmnga/cyberagent-calm2-7b-chat-dpo-experimental-gguf/resolve/main/cyberagent-calm2-7b-chat-dpo-experimental-q5_K_S.gguf?download=true \
    -O ./models/cyberagent-calm2-7b-chat-dpo-experimental-q5_K_S.gguf
fi

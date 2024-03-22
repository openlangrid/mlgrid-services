
WEIGHTS=(.gitattributes
    config.json
    pytorch_model-00001-of-00002.bin
    pytorch_model-00002-of-00002.bin
    pytorch_model.bin.index.json
    special_tokens_map.json
    spiece.model
    tokenizer_config.json
    )
DIR=./pretrained_models/t5_ckpts/t5-v1_1-xxl

for W in "${WEIGHTS[@]}"; do
  if [ ! -e ${DIR}/${W} ]; then
    wget https://huggingface.co/DeepFloyd/t5-v1_1-xxl/resolve/main/${W}?download=true \
      -O ${DIR}/${W}
  fi
done

#https://huggingface.co/DeepFloyd/t5-v1_1-xxl/resolve/main/?download=true
#https://huggingface.co/DeepFloyd/t5-v1_1-xxl/resolve/main/config.json?download=true
#https://huggingface.co/DeepFloyd/t5-v1_1-xxl/resolve/main/pytorch_model-00001-of-00002.bin?download=true
#https://huggingface.co/DeepFloyd/t5-v1_1-xxl/resolve/main/pytorch_model-00002-of-00002.bin?download=true
#https://huggingface.co/DeepFloyd/t5-v1_1-xxl/resolve/main/pytorch_model.bin.index.json?download=true
#https://huggingface.co/DeepFloyd/t5-v1_1-xxl/resolve/main/special_tokens_map.json?download=true
#https://huggingface.co/DeepFloyd/t5-v1_1-xxl/resolve/main/spiece.model?download=true
#https://huggingface.co/DeepFloyd/t5-v1_1-xxl/resolve/main/tokenizer_config.json?download=true


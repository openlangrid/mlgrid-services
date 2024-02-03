from llama_cpp import Llama

llm = Llama(
    model_path="./models/karakuri-lm-70b-chat-v0.1-q5_K_S.gguf",
    n_gpu_layers=-1)
prompt="<s>[INST] まどか☆マギカで誰が一番かわいい？その理由も説明して [ATTR] helpfulness: 4 correctness: 4 coherence: 4 complexity: 4 verbosity: 4 quality: 4 toxicity: 0 humor: 0 creativity: 0 [/ATTR] [/INST]"
output = llm(prompt, max_tokens=512)
print(output["choices"][0]["text"])

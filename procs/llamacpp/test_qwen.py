from llama_cpp import Llama

llm = Llama(
    model_path="./models/qwen1_5-72b-chat-q4_k_m.gguf",
    n_gpu_layers=-1)
prompt="<|im_start|>user\nまどか☆マギカで誰が一番かわいい？その理由も説明して<|im_end|>\n<|im_start|>assistant\n"
output = llm(prompt, max_tokens=512)
print(output["choices"][0]["text"])

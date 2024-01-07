import torch
from torch.cuda import OutOfMemoryError
from transformers import AutoModelForCausalLM, AutoTokenizer
from gpuinfo import get_gpu_properties
import json

model_names = [
#    "elyza/ELYZA-japanese-Llama-2-13b",
#    "elyza/ELYZA-japanese-Llama-2-13b-instruct",
#    "elyza/ELYZA-japanese-Llama-2-13b-fast",
    "elyza/ELYZA-japanese-Llama-2-13b-fast-instruct",
    ]
bfloat16Types = set(["elyza/ELYZA-japanese-Llama-2-13b-fast-instruct",
                    "elyza/ELYZA-japanese-Llama-2-13b-instruct"])

def generate(tokenizer, model, text):
    token_ids = tokenizer.encode(text, add_special_tokens=False, return_tensors="pt")
    with torch.no_grad():
        output_ids = model.generate(
            token_ids.to(model.device),
            max_new_tokens=256,
            pad_token_id=tokenizer.pad_token_id,
            eos_token_id=tokenizer.eos_token_id,
        )
    print(json.dumps(get_gpu_properties()))
    output = tokenizer.decode(output_ids.tolist()[0], skip_special_tokens=True)
    return output

text = "自然言語処理とは、"
tokenizers = []
models = []
try:
    for m in model_names:
        print("### load tokenizer and model")
        tokenizer = AutoTokenizer.from_pretrained(m)
        model = AutoModelForCausalLM.from_pretrained(
            m,
            torch_dtype=torch.bfloat16 if m in bfloat16Types else torch.float16,
            use_cache=True,
            device_map="cuda",
            low_cpu_mem_usage=True,
        )
        print("### do inference")
        print(generate(tokenizer, model, text))
        tokenizers.append(tokenizer)
        models.append(model)
    print("#### first process done")
except OutOfMemoryError as e:
    print("#### OutOfMemory Error!!")

print("#### process again with loaded models")
for tokenizer, model in zip(tokenizers, models):
    print("### do inference")
    print(generate(tokenizer, model, text))
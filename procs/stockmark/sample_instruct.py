import torch
from transformers import AutoModelForCausalLM, AutoTokenizer

model = AutoModelForCausalLM.from_pretrained("stockmark/stockmark-13b-instruct", device_map="auto", torch_dtype=torch.bfloat16)
tokenizer = AutoTokenizer.from_pretrained("stockmark/stockmark-13b-instruct")

instruction = "自然言語処理とは？"

prompt = f"""### Input:
{instruction}

### Output:
"""

inputs = tokenizer(prompt, return_tensors="pt").to(model.device)
with torch.no_grad():
    tokens = model.generate(
        **inputs,
        max_new_tokens=128,
        do_sample=True,
        temperature=0.7
    )
    
output = tokenizer.decode(tokens[0], skip_special_tokens=True)
print(output)

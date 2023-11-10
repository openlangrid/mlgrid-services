import torch
from transformers import AutoModelForCausalLM, AutoTokenizer

# For A100 or H100 GPU
model = AutoModelForCausalLM.from_pretrained(
  "stockmark/stockmark-13b",
  device_map="auto",
  torch_dtype=torch.bfloat16)

# If you use a T4 or V100 GPU, please load a model in 8 bit with the below code.
# To do so, you need to install `bitsandbytes` via `pip install bitsandbytes`.
# model = AutoModelForCausalLM.from_pretrained("stockmark/stockmark-13b", device_map={"": 0}, load_in_8bit=True)

tokenizer = AutoTokenizer.from_pretrained("stockmark/stockmark-13b")

inputs = tokenizer("自然言語処理とは", return_tensors="pt").to(model.device)
with torch.no_grad():
    tokens = model.generate(
        **inputs,
        max_new_tokens=128,
        do_sample=True,
        temperature=0.7
    )
    
output = tokenizer.decode(tokens[0], skip_special_tokens=True)
print(output)

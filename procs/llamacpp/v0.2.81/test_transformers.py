import sys
modelId = sys.argv[1] if len(sys.argv) >= 2 else "google/gemma-2-9b-it"


from transformers import AutoTokenizer, AutoModelForCausalLM
import transformers
import torch

model_id = modelId
dtype = torch.bfloat16

tokenizer = AutoTokenizer.from_pretrained(model_id)
model = AutoModelForCausalLM.from_pretrained(
    model_id,
    device_map="cuda",
    torch_dtype=dtype,)

chat = [
    { "role": "user", "content": "Write a hello world program" },
]
prompt = tokenizer.apply_chat_template(chat, tokenize=False, add_generation_prompt=True)
inputs = tokenizer.encode(prompt, add_special_tokens=False, return_tensors="pt")
outputs = model.generate(input_ids=inputs.to(model.device), max_new_tokens=1024)
output = tokenizer.decode(outputs[0][len(inputs[0]):]).removesuffix(tokenizer.eos_token).rstrip()
for t in tokenizer.additional_special_tokens:
    output = output.removesuffix(t).rstrip()
print(output)

from gpuinfo import get_gpu_properties
import json
props = get_gpu_properties()
print(f"ok {json.dumps(props)}", flush=True)

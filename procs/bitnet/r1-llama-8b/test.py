import torch
from transformers import AutoModelForCausalLM, AutoTokenizer

#model_id = "microsoft/bitnet-b1.58-2B-4T"
model_id = "codys12/bitnet-r1-llama-8b"

# Load tokenizer and model
model = AutoModelForCausalLM.from_pretrained(
    model_id,
    device_map="cuda"
)
tokenizer = AutoTokenizer.from_pretrained(model_id, padding_side="left")

# Apply the chat template
messages = [
    {"role": "system", "content": "You are a helpful AI assistant. Please anwer in Japanese."},
    {"role": "user", "content": "What are some recommended sightseeing spots in Kyoto?"},
]
prompt = tokenizer.apply_chat_template(messages, tokenize=False, add_generation_prompt=True)
chat_input = tokenizer(prompt, return_tensors="pt").to(model.device)

# Generate response
chat_outputs = model.generate(**chat_input, max_new_tokens=50)
response = tokenizer.decode(
    chat_outputs[0][chat_input['input_ids'].shape[-1]:],
    skip_special_tokens=True) # Decode only the response part
print("\nAssistant Response:", response)

from gpuinfo import get_gpu_properties
import json
props = get_gpu_properties()
print(f"ok {json.dumps(props)}", flush=True)

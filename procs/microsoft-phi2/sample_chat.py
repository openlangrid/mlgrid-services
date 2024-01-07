import torch
from transformers import AutoModelForCausalLM, AutoTokenizer

torch.set_default_device("cuda")

model = AutoModelForCausalLM.from_pretrained(
    "microsoft/phi-2", torch_dtype="auto",
    device_map="auto", trust_remote_code=True)
tokenizer = AutoTokenizer.from_pretrained("microsoft/phi-2", trust_remote_code=True)

text = "I don't know why, I'm struggling to maintain focus while studying. Any suggestions?"
prompt = f"Alice: {text}\nBob: "

inputs = tokenizer(prompt, return_tensors="pt", return_attention_mask=False)

outputs = model.generate(**inputs, max_length=200)
text = tokenizer.batch_decode(
    outputs
    )[0]

inputTokenLen = len(inputs["input_ids"][0])
#print(tokenizer.decode(outputs[0][inputTokenLen:]))
print(tokenizer.batch_decode(outputs)[0])
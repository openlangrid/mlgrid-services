import torch
from transformers import AutoModelForCausalLM, AutoTokenizer

torch.set_default_device("cuda")

model = AutoModelForCausalLM.from_pretrained(
    "microsoft/phi-2", torch_dtype="auto",
    device_map="auto", trust_remote_code=True)
tokenizer = AutoTokenizer.from_pretrained("microsoft/phi-2", trust_remote_code=True)

text = "Write a detailed analogy between mathematics and a lighthouse."
#text = "数学と灯台の共通点を日本語で詳細に記述してください."
#text = "アルパカについて教えてください。"
prompt = f"Instruct: {text}\nOutput: "
prompt = text

inputs = tokenizer(prompt, return_tensors="pt", return_attention_mask=False)

outputs = model.generate(**inputs, max_length=200)
#text = tokenizer.batch_decode(outputs)[0]
#print(text)

inputTokenLen = len(inputs["input_ids"][0])
#print(tokenizer.decode(outputs[0][inputTokenLen:]).split("<|endoftext|>")[0])
print(tokenizer.batch_decode(outputs)[0])

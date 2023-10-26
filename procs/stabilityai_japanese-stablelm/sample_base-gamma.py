from transformers import AutoModelForCausalLM, AutoTokenizer
tokenizer = AutoTokenizer.from_pretrained("stabilityai/japanese-stablelm-base-gamma-7b")
model = AutoModelForCausalLM.from_pretrained(
  "stabilityai/japanese-stablelm-base-gamma-7b",
  torch_dtype="auto",
)
model.cuda()
inputs = tokenizer("AI で科学研究を加速するには、", return_tensors="pt").to("cuda")
tokens = model.generate(
  **inputs,
  max_new_tokens=64,
  temperature=0.75,
  top_p=0.95,
  do_sample=True,
)
print(tokenizer.decode(tokens[0], skip_special_tokens=True))

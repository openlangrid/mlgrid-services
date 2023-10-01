from transformers import AutoTokenizer, AutoModelForCausalLM
tokenizer = AutoTokenizer.from_pretrained(
    "pfnet/plamo-13b",
    trust_remote_code=True)
model = AutoModelForCausalLM.from_pretrained(
    "pfnet/plamo-13b",
    device_map="auto",
    trust_remote_code=True)
text = "これからの人工知能技術は"
input_ids = tokenizer(text, return_tensors="pt").input_ids
input_ids.to(model.device)
generated_tokens = model.generate(
    inputs=input_ids,
    max_new_tokens=32,
    do_sample=True,
    top_k=50,
    top_p=0.95,
    temperature=1.0,
)[0]
generated_text = tokenizer.decode(generated_tokens[input_ids.shape[1]:])
print(generated_text)

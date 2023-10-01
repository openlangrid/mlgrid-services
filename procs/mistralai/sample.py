from transformers import AutoTokenizer, AutoModelForCausalLM
tokenizer = AutoTokenizer.from_pretrained("mistralai/Mistral-7B-v0.1")
model = AutoModelForCausalLM.from_pretrained(
    "mistralai/Mistral-7B-v0.1",
    device_map="auto",
    )
text = "これからの人工知能技術は"
input_ids = tokenizer(text, return_tensors="pt").input_ids
input_ids = input_ids.to(model.device)
generated_tokens = model.generate(
    inputs=input_ids,
    max_new_tokens=512,
    do_sample=True)[0]
generated_text = tokenizer.decode(generated_tokens[input_ids.shape[1]:])
print(generated_text)

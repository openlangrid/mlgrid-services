from transformers import AutoModelForCausalLM, AutoTokenizer

model_id = "openai/gpt-oss-20b"

tokenizer = AutoTokenizer.from_pretrained(model_id)
model = AutoModelForCausalLM.from_pretrained(
    model_id,
    device_map="auto",
    torch_dtype="auto",
    # Optimize MoE layers with downloadable` MegaBlocksMoeMLP
    use_kernels=True,
)

messages = [
    {"role": "user", "content": "京都の夏におすすめの観光地を教えてください。"},
]

inputs = tokenizer.apply_chat_template(
    messages,
    add_generation_prompt=True,
    tokenize=True,
    return_tensors="pt",
    return_dict=True,
).to(model.device)

generated = model.generate(**inputs, max_new_tokens=2048)
print(tokenizer.decode(generated[0][inputs["input_ids"].shape[-1]:]))

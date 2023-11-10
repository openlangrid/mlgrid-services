import transformers
from transformers import AutoModelForCausalLM, AutoTokenizer, TextStreamer

assert transformers.__version__ >= "4.34.1"

model = AutoModelForCausalLM.from_pretrained("cyberagent/calm2-7b", device_map="auto", torch_dtype="auto")
tokenizer = AutoTokenizer.from_pretrained("cyberagent/calm2-7b")
streamer = TextStreamer(tokenizer, skip_prompt=True, skip_special_tokens=True)

prompt = "AIによって私達の暮らしは、"

token_ids = tokenizer.encode(prompt, return_tensors="pt")
output_ids = model.generate(
    input_ids=token_ids.to(model.device),
    max_new_tokens=100,
    do_sample=True,
    temperature=0.9,
    streamer=streamer,
)

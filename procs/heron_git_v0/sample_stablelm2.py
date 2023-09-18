import torch
from heron.models.video_blip import VideoBlipForConditionalGeneration, VideoBlipProcessor
from transformers import LlamaTokenizer

device_id = 0
device = f"cuda:{device_id}"

max_length = 512
MODEL_NAME = "turing-motors/heron-chat-blip-ja-stablelm-base-7b-v0"
    
model = VideoBlipForConditionalGeneration.from_pretrained(
    MODEL_NAME, torch_dtype=torch.float16, ignore_mismatched_sizes=True
)

model = model.half()
model.eval()
model.to(device)

# prepare a processor
processor = VideoBlipProcessor.from_pretrained("Salesforce/blip2-opt-2.7b")
tokenizer = LlamaTokenizer.from_pretrained("novelai/nerdstash-tokenizer-v1", additional_special_tokens=['▁▁'])
processor.tokenizer = tokenizer

import requests
from PIL import Image

# prepare inputs
url = "https://www.barnorama.com/wp-content/uploads/2016/12/03-Confusing-Pictures.jpg"
image = Image.open(requests.get(url, stream=True).raw)

text = f"##human: この画像の面白い点は何ですか?\n##gpt: "

# do preprocessing
inputs = processor(
    text=text,
    images=image,
    return_tensors="pt",
    truncation=True,
)

inputs = {k: v.to(device) for k, v in inputs.items()}
inputs["pixel_values"] = inputs["pixel_values"].to(device, torch.float16)

# set eos token
eos_token_id_list = [
    processor.tokenizer.pad_token_id,
    processor.tokenizer.eos_token_id,
    int(tokenizer.convert_tokens_to_ids("##"))
]

# do inference
with torch.no_grad():
    out = model.generate(**inputs, max_length=256, do_sample=False, temperature=0., eos_token_id=eos_token_id_list, no_repeat_ngram_size=2)

# print result
print(processor.tokenizer.batch_decode(out))

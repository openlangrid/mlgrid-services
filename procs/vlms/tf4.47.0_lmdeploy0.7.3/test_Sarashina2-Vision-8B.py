import requests
from PIL import Image
from transformers import AutoModelForCausalLM, AutoProcessor

# Define model path
model_path = "sbintuitions/sarashina2-vision-8b"

# Load model and processor
processor = AutoProcessor.from_pretrained(model_path, trust_remote_code=True)
model = AutoModelForCausalLM.from_pretrained(
    model_path,
    device_map="cuda",
    torch_dtype="auto",
    trust_remote_code=True,
)

#message = [{"role": "user", "content": "この写真に写っているもので、最も有名と考えられる建築物は何でどこに写っていますか？"}]
#message = [{"role": "user", "content": "これはアマチュア写真家が撮影した写真です。モチベーションが上がるように、写真を褒めてください。"}]
message = [{"role": "user", "content": "明るく親しげな口調で、具体的に絵を褒めて、上達のアドバイスをしてください。"}]
text_prompt = processor.apply_chat_template(message, add_generation_prompt=True)
"""text_prompt: <s><|prefix|><|file|><|suffix|>A chat between a curious human and an artificial intelligence assistant. The assistant gives helpful, detailed, and polite answers to the human's questions.

### Human: この写真に写っているもので、最も有名と考えられる建築物は何でどこに写っていますか？
### Assistant:"""

sample_image_url = "https://huggingface.co/sbintuitions/sarashina2-vision-8b/resolve/main/sample.jpg"
#sample_image = requests.get(sample_image_url, stream=True).raw
sample_image = "./dora.png"
image = Image.open(sample_image).convert("RGB")
inputs = processor(
    text=[text_prompt],
    images=[image],
    padding=True,
    return_tensors="pt",
)
inputs = inputs.to("cuda")
stopping_criteria = processor.get_stopping_criteria(["\n###"])

# Inference: Generation of the output
output_ids = model.generate(
    **inputs,
    max_new_tokens=128,
    temperature=0.0,
    do_sample=False,
    stopping_criteria=stopping_criteria,
)
generated_ids = [
    output_ids[len(input_ids) :] for input_ids, output_ids in zip(inputs.input_ids, output_ids)
]
output_text = processor.batch_decode(
    generated_ids, skip_special_tokens=True, clean_up_tokenization_spaces=True
)
print(output_text[0])
"""この写真に写っているもので、最も有名と考えられる建築物は東京タワーです。東京タワーは、東京のランドマークであり、この写真では、高層ビル群の向こう側に写っています。"""

from gpuinfo import get_gpu_properties
import json
props = get_gpu_properties()
print(f"ok {json.dumps(props)}", flush=True)

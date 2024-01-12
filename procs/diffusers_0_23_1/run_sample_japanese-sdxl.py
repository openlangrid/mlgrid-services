from diffusers import DiffusionPipeline
import torch

pipeline = DiffusionPipeline.from_pretrained(
    "stabilityai/japanese-stable-diffusion-xl",
    trust_remote_code=True
)
pipeline.to("cuda")

# if using torch < 2.0
# pipeline.enable_xformers_memory_efficient_attention()

prompt = "柴犬、カラフルアート"

image = pipeline(prompt=prompt).images[0]
image.save("sample/japanese-sdxl_output.jpg")

from gpuinfo import get_gpu_properties
import json
props = get_gpu_properties()
print(f"ok {json.dumps(props)}", flush=True)

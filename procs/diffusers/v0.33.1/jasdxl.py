from diffusers import DiffusionPipeline
import torch

pipeline = DiffusionPipeline.from_pretrained(
    "stabilityai/japanese-stable-diffusion-xl", trust_remote_code=True
)
pipeline.to("cuda")

# if using torch < 2.0
# pipeline.enable_xformers_memory_efficient_attention()

prompt = "柴犬、カラフルアート"

image = pipeline(prompt=prompt).images[0]

image.save("output_sdxl.png")
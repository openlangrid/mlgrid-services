import os, torch
from diffusers import StableDiffusionPipeline

TOKEN = os.environ['HUGGINGFACE_TOKEN']

pipe = StableDiffusionPipeline.from_pretrained(
    "runwayml/stable-diffusion-v1-5", torch_dtype=torch.float16, revision="fp16",
    use_auth_token=TOKEN)
pipe = pipe.to("cuda")

prompt = "a photo of an astronaut riding a horse on mars"
image = pipe(prompt).images[0]  

image.save(f"out.png")

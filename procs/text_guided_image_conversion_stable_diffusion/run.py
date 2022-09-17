import argparse, os

TOKEN = os.environ['HUGGINGFACE_TOKEN']

parser = argparse.ArgumentParser()
parser.add_argument("prompt", type=str, default="sunset over a lake in the mountains")
parser.add_argument("input_file", type=str)
parser.add_argument("output_path",type=str, default="out")
args = parser.parse_args()
prompt = args.prompt
input_file = args.input_file
output_path = args.output_path


from diffusers import StableDiffusionImg2ImgPipeline
import torch
from torch import autocast

pipe = StableDiffusionImg2ImgPipeline.from_pretrained(
    "CompVis/stable-diffusion-v1-4",
    revision="fp16", 
    torch_dtype=torch.float16,
    use_auth_token=TOKEN
).to("cuda")


from PIL import Image

with Image.open(input_file) as im:
    with autocast("cuda"):
        image = pipe(prompt=prompt, init_image=im, strength=0.75, guidance_scale=7.5).images[0]
        image.save(f"{output_path}.png")
with open(f"{output_path}.txt", 'w', encoding='UTF-8') as f:
    f.write(prompt)

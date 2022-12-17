import argparse, os

TOKEN = os.environ['HUGGINGFACE_TOKEN']

parser = argparse.ArgumentParser()
parser.add_argument("input_file", type=str)
parser.add_argument("prompt", type=str, default="sunset over a lake in the mountains")
parser.add_argument("num_of_samples", nargs="?", type=int, default=1)
parser.add_argument("output_path",type=str, default="out")
args = parser.parse_args()
prompt = args.prompt
input_file = args.input_file
output_path = args.output_path
num_of_samples = args.num_of_samples


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
        images = pipe(prompt=[prompt]*num_of_samples, init_image=im, strength=0.75, guidance_scale=7.5).images

with open(f"{output_path}.txt", 'w', encoding='UTF-8') as f:
    f.write(prompt)

for i in range(num_of_samples):
    image = images[i]
    image.save(f"{output_path}_{i}.png")

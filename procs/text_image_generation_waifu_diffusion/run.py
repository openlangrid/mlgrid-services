import argparse, os

parser = argparse.ArgumentParser()
parser.add_argument("prompt", type=str, default="sunset over a lake in the mountains")
parser.add_argument("num_of_samples", type=int, default=0)
parser.add_argument("outpath",type=str, default="out")
args = parser.parse_args()
prompt = args.prompt
num_of_samples = args.num_of_samples
outpath = args.outpath


from diffusers import StableDiffusionPipeline, DDIMScheduler
import torch
from torch import autocast

pipe = StableDiffusionPipeline.from_pretrained(
    "hakurei/waifu-diffusion",
    torch_dtype=torch.float16,
    revision="fp16",
    scheduler=DDIMScheduler(
        beta_start=0.00085,
        beta_end=0.012,
        beta_schedule="scaled_linear",
        clip_sample=False,
        set_alpha_to_one=False,
        )
    ).to("cuda")

with autocast("cuda"):
    images = pipe([prompt] * num_of_samples, guidance_scale=7.5).images
for i in range(num_of_samples):
    image = images[i]
    image.save(f"{outpath}_{i}.png")
    print(f"{outpath}_{i}.png")


with open(f"{outpath}.txt", 'w', encoding='UTF-8') as f:
    f.write(prompt)

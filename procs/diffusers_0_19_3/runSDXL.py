# model paths
# "CompVis/stable-diffusion-v1-4", "rinna/japanese-stable-diffusion",
# "doohickey/trinart-waifu-diffusion-50-50", "sd-dreambooth-library/disco-diffusion-style"
# "hakurei/waifu-diffusion", "naclbit/trinart_stable_diffusion_v2"(diffusers-60k, diffusers-95k / diffusers-115k)

# parse args
import argparse
parser = argparse.ArgumentParser()
parser.add_argument("prompt", nargs="?", type=str, default="sunset over a lake in the mountains")
parser.add_argument("numOfSamples", nargs="?", type=int, default=1)
parser.add_argument("outPathPrefix",type=str, default="out")
parser.add_argument("--modelPath", type=str, default="runwayml/stable-diffusion-v1-5")
parser.add_argument("--modelRevision", type=str, default=None)
parser.add_argument("--modelTorchDType", type=str, default=None)
parser.add_argument("--initImage", type=str, default=None)
parser.add_argument("--negativePrompt", type=str, default=None)
args = parser.parse_args()

# construct opts
modelPath = args.modelPath
outPathPrefix = args.outPathPrefix
prompt = args.prompt

import torch
from diffusers import DiffusionPipeline

base = DiffusionPipeline.from_pretrained(
    modelPath,
    torch_dtype=torch.float16, variant="fp16",
    use_safetensors=True)
base.to("cuda")
images = base(prompt=prompt).images

# save images
for i in range(len(images)):
    image = images[i]
    image.save(f"{outPathPrefix}_{i}.png")

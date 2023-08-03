
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
    torch_dtype=torch.float16, variant="fp16", use_safetensors=True)
base.to("cuda")
refiner = DiffusionPipeline.from_pretrained(
    "stabilityai/stable-diffusion-xl-refiner-1.0",
    torch_dtype=torch.float16, variant="fp16", use_safetensors=True,
    text_encoder_2=base.text_encoder_2, vae=base.vae)
refiner.to("cuda")

n_steps = 40
high_noise_frac = 0.8
images = base(
    prompt=prompt,
    num_inference_steps=n_steps,
    denoising_end=high_noise_frac,
    output_type="latent"
    ).images
images = refiner(
    prompt=prompt,
    image=images,
    num_inference_steps=n_steps,
    denoising_start=high_noise_frac
    ).images

# save images
for i in range(len(images)):
    image = images[i]
    image.save(f"{outPathPrefix}_{i}.png")

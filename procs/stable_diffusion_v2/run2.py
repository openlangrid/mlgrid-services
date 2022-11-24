# parse args
import argparse, os
parser = argparse.ArgumentParser()
parser.add_argument("prompt", nargs="?", type=str, default="sunset over a lake in the mountains")
parser.add_argument("numOfSamples", nargs="?", type=int, default=1)
parser.add_argument("outPathPrefix",type=str, default="out")
parser.add_argument("--modelPath", type=str, default="stabilityai/stable-diffusion-2")
parser.add_argument("--modelRevision", type=str, default=None)
parser.add_argument("--modelTorchDType", type=str, default=None)
parser.add_argument("--initImage", type=str, default=None)
parser.add_argument("--negativePrompt", type=str, default=None)
args = parser.parse_args()

from diffusers import StableDiffusionPipeline, EulerDiscreteScheduler
import torch


# Use the Euler scheduler here instead
scheduler = EulerDiscreteScheduler.from_pretrained(
    args.modelPath, subfolder="scheduler")
pipe = StableDiffusionPipeline.from_pretrained(
    args.modelPath, scheduler=scheduler, revision="fp16", torch_dtype=torch.float16)
pipe = pipe.to("cuda")

prompt = "a photo of an astronaut riding a horse on mars"
image = pipe(args.prompt, height=768, width=768).images[0]
    
image.save("astronaut_rides_horse.png")

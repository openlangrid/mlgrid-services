# model paths
# "CompVis/stable-diffusion-v1-4", "rinna/japanese-stable-diffusion",
# "doohickey/trinart-waifu-diffusion-50-50", "sd-dreambooth-library/disco-diffusion-style"
# "hakurei/waifu-diffusion", "naclbit/trinart_stable_diffusion_v2"(diffusers-60k, diffusers-95k / diffusers-115k)


# get env var
import os

TOKEN = os.environ['HUGGINGFACE_TOKEN']


# parse args
import argparse, os
parser = argparse.ArgumentParser()
parser.add_argument("prompt", nargs="?", type=str, default="sunset over a lake in the mountains")
parser.add_argument("numOfSamples", nargs="?", type=int, default=1)
parser.add_argument("outPathPrefix",type=str, default="out")
parser.add_argument("--modelPath", type=str, default="runwayml/stable-diffusion-v1-5")
parser.add_argument("--modelRevision", type=str, default=None)
parser.add_argument("--modelTorchDType", type=str, default=None)
parser.add_argument("--initImage", type=str)
parser.add_argument("--negativePrompt", type=str)
args = parser.parse_args()

# construct opts
import torch
modelPath = args.modelPath
outPathPrefix = args.outPathPrefix
modelOpts = {
    "revision": args.modelRevision,
    "torch_dtype": args.modelTorchDType
}
pipeOpts = {
    "prompt": args.prompt,
    "nagetive_prompt": args.negativePrompt,
    "init_image": args.initImage
}

# adjust options before save
if modelOpts["revision"] == None:
    if modelPath == "naclbit/trinart_stable_diffusion_v2":
        modelOpts["revision"] = "diffusers-60k"
    if modelPath == "CompVis/stable-diffusion-v1-4" or modelPath == "runwayml/stable-diffusion-v1-5":
        modelOpts["revision"] = "fp16"
if modelOpts["torch_dtype"] == None:
    if modelPath == "CompVis/stable-diffusion-v1-4" or modelPath == "runwayml/stable-diffusion-v1-5":
        modelOpts["torch_dtype"] = torch.float16

# write options
import json
class Encoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, torch.dtype):
            return str(obj)
        return json.JSONEncoder.default(self, obj)
with open(f"{outPathPrefix}.modelOpts.txt", 'w', encoding='UTF-8') as f:
    modelOpts["modelPath"] = modelPath
    f.write(json.dumps(modelOpts, indent=2, cls=Encoder))
with open(f"{outPathPrefix}.pipeOpts.txt", 'w', encoding='UTF-8') as f:
    f.write(json.dumps(pipeOpts, indent=2, cls=Encoder))

# adjust options after save
from PIL import Image
modelOpts["use_auth_token"] = TOKEN
pipeOpts["prompt"] = [args.prompt] * args.numOfSamples
pipeOpts["init_image"] = Image.open(pipeOpts["init_image"]) if pipeOpts["init_image"] != None else None


# prepare model
from diffusers import StableDiffusionPipeline, StableDiffusionImg2ImgPipeline
from torch import autocast

del modelOpts["modelPath"]
if pipeOpts["init_image"]:
    pipe = StableDiffusionImg2ImgPipeline.from_pretrained(
        modelPath, **modelOpts)
else:
    pipe = StableDiffusionPipeline.from_pretrained(
        modelPath, **modelOpts)
#pipe.enable_attention_slicing()
pipe.to("cuda")


# run
with autocast("cuda"):
    images = pipe(**pipeOpts).images


# save images
for i in range(len(pipeOpts["prompt"])):
    image = images[i]
    image.save(f"{outPathPrefix}_{i}.png")


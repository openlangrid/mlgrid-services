# model paths
# "CompVis/stable-diffusion-v1-4", "rinna/japanese-stable-diffusion",
# "doohickey/trinart-waifu-diffusion-50-50", "sd-dreambooth-library/disco-diffusion-style"
# "hakurei/waifu-diffusion", "naclbit/trinart_stable_diffusion_v2"(diffusers-60k, diffusers-95k / diffusers-115k)


# get env var
import os

TOKEN = os.environ['HUGGINGFACE_TOKEN']


# load defaults
import torch
import yaml
def convDict(value):
    return dict(map(convItem, value.items()))

def convItem(item):
    [key, value] = item
    if isinstance(value, dict):
        return (key, convDict(value))
    if key == "torch_dtype":
        return (key, eval(value))
    return (key, value)

with open('defaults.yml', 'r') as yml:
    config = yaml.safe_load(yml)["defaults"]
    config = convDict(config)


# parse args
import argparse, os
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
import torch
modelPath = args.modelPath
outPathPrefix = args.outPathPrefix

modelOpts = config[args.modelPath] if args.modelPath in config else {}
if args.modelRevision: modelOpts["revision"] = args.modelRevision
if args.modelTorchDType: modelOpts["torch_dtype"] = args.modelTorchDType

pipeOpts = {
    "prompt": args.prompt,
    "nagetive_prompt": args.negativePrompt,
    "init_image": args.initImage,
    "num_inference_steps": 25
}

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
del modelOpts["modelPath"]
if pipeOpts["init_image"] != None:
    from diffusers import StableDiffusionImg2ImgPipeline
    pipe = StableDiffusionImg2ImgPipeline.from_pretrained(
        modelPath, **modelOpts)
else:
    del pipeOpts["init_image"]
    if pipeOpts["nagetive_prompt"] == None: del pipeOpts["nagetive_prompt"]
    from diffusers import StableDiffusionPipeline
    pipe = StableDiffusionPipeline.from_pretrained(
        modelPath, **modelOpts)
    from diffusers import EulerDiscreteScheduler
    pipe.scheduler = EulerDiscreteScheduler.from_config(pipe.scheduler.config)
#pipe.enable_attention_slicing()
pipe = pipe.to("cuda")
from diffusers.utils.import_utils import is_xformers_available
if is_xformers_available():
    try:
        pipe.enable_xformers_memory_efficient_attention(True)
    except Exception as e:
        logger.warning(
            "Could not enable memory efficient attention. Make sure xformers is installed"
            f" correctly and a GPU is available: {e}"
        )

# run
#with autocast("cuda"):
def null_safety(images, **kwargs):
    return images, False
pipe.safety_checker = null_safety
images = pipe(**pipeOpts).images


# save images
for i in range(len(pipeOpts["prompt"])):
    image = images[i]
    image.save(f"{outPathPrefix}_{i}.png")

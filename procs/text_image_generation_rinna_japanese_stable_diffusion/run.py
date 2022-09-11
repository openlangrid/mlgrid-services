import argparse, os

TOKEN = os.environ['HUGGINGFACE_TOKEN']

parser = argparse.ArgumentParser()
parser.add_argument("prompt", type=str, default="sunset over a lake in the mountains")
parser.add_argument("outpath",type=str, default="out")
args = parser.parse_args()
prompt = args.prompt
outpath = args.outpath


import torch
from torch import autocast
from diffusers import LMSDiscreteScheduler
from japanese_stable_diffusion import JapaneseStableDiffusionPipeline

model_id = "rinna/japanese-stable-diffusion"
device = "cuda" if torch.cuda.is_available() else "cpu"
# Use the K-LMS scheduler here instead
scheduler = LMSDiscreteScheduler(
    beta_start=0.00085, beta_end=0.012,
    beta_schedule="scaled_linear",
    num_train_timesteps=1000
)
pipe = JapaneseStableDiffusionPipeline.from_pretrained(
    pretrained_model_name_or_path=model_id,
    scheduler=scheduler,
    torch_dtype=torch.float16,
    use_auth_token=TOKEN
).to(device)

# the number of output images. If you encounter Out Of Memory error, decrease this number.
n_samples = 1 #@param{type: 'integer'}
# `classifier-free guidance scale` adjusts how much the image will be like your prompt. Higher values keep your image closer to your prompt.
guidance_scale = 7.5 #@param {type:"number"}
# How many steps to spend generating (diffusing) your image.
steps = 50 #@param{type: 'integer'}
# The width of the generated image.
width = 512 #@param{type: 'integer'}
# The height of the generated image.
height = 512 #@param{type: 'integer'}
# The seed used to generate your image. Enable to manually set a seed.
seed = 'random' #@param{type: 'string'}

if seed == "random":
    generator = None
else:
    generator = torch.Generator(device=device).manual_seed(int(seed))

with autocast(device):
    images = pipe(
        prompt=[prompt] * n_samples,
        guidance_scale=guidance_scale,
        num_inference_steps=steps,
        height=height,
        width=width,
        generator=generator
    )["sample"]

image = images[0]
image.save(f"{outpath}.png")

with open(f"{outpath}.txt", 'w', encoding='UTF-8') as f:
    f.write(prompt)

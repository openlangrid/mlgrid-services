import argparse, os

TOKEN = os.environ['HUGGINGFACE_TOKEN']

parser = argparse.ArgumentParser()
parser.add_argument("prompt", type=str, default="sunset over a lake in the mountains")
parser.add_argument("outpath",type=str, default="out")
args = parser.parse_args()
prompt = args.prompt
outpath = args.outpath


from diffusers import StableDiffusionPipeline

pipe = StableDiffusionPipeline.from_pretrained("CompVis/stable-diffusion-v1-4", use_auth_token=TOKEN)
pipe.to("cuda")

image = pipe(prompt)["sample"][0]
image.save(f"{outpath}.png")

with open(f"{outpath}.txt", 'w', encoding='UTF-8') as f:
    f.write(prompt)

import argparse, os

TOKEN = os.environ['HUGGINGFACE_TOKEN']

parser = argparse.ArgumentParser()
parser.add_argument("prompt", type=str, default="sunset over a lake in the mountains")
parser.add_argument("num_of_samples", type=int, default=0)
parser.add_argument("outpath",type=str, default="out")
args = parser.parse_args()
prompt = args.prompt
num_of_samples = args.num_of_samples
outpath = args.outpath


from diffusers import StableDiffusionPipeline

pipe = StableDiffusionPipeline.from_pretrained("CompVis/stable-diffusion-v1-4", use_auth_token=TOKEN)
pipe.to("cuda")

images = pipe([prompt] * num_of_samples)["sample"]
for i in range(num_of_samples):
    image = images[i]
    image.save(f"{outpath}_{i}.png")

with open(f"{outpath}.txt", 'w', encoding='UTF-8') as f:
    f.write(prompt)

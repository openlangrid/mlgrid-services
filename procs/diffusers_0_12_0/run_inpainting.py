import PIL
import requests
import torch
from io import BytesIO

from diffusers import StableDiffusionInpaintPipeline


def download_image(url):
    content = None
    if url.startswith("http"):
        content = BytesIO(requests.get(url).content)
    else:
        content = url
    return PIL.Image.open(content).convert("RGB")

dir = "./inpainting/apples2"
img_url = f"{dir}/init.png"
#"https://raw.githubusercontent.com/CompVis/latent-diffusion/main/data/inpainting_examples/overture-creations-5sI6fQgYIuo.png"
mask_url = f"{dir}/mask.png"
#"https://raw.githubusercontent.com/CompVis/latent-diffusion/main/data/inpainting_examples/overture-creations-5sI6fQgYIuo_mask.png"

init_image = download_image(img_url).resize((512, 512))
mask_image = download_image(mask_url).resize((512, 512))

pipe = StableDiffusionInpaintPipeline.from_pretrained(
    "runwayml/stable-diffusion-inpainting",
    revision="fp16",
    torch_dtype=torch.float16,
)
pipe = pipe.to("cuda")

#prompt = "Face of a yellow cat, high resolution, sitting on a park bench"
prompt = "face of Donald Trump, high resolution, many faces on the tree"
image = pipe(prompt=prompt, image=init_image, mask_image=mask_image).images[0]

image.save(f"{dir}/out.png")

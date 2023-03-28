import PIL
import requests
import torch
from diffusers import StableDiffusionInstructPix2PixPipeline

model_id = "timbrooks/instruct-pix2pix"
pipe = StableDiffusionInstructPix2PixPipeline.from_pretrained(
    model_id, torch_dtype=torch.float16, safety_checker=None
).to("cuda")

url = "https://raw.githubusercontent.com/timothybrooks/instruct-pix2pix/main/imgs/example.jpg"


def download_image(url):
    image = PIL.Image.open(requests.get(url, stream=True).raw)
    image = PIL.ImageOps.exif_transpose(image)
    image = image.convert("RGB")
    return image


image = download_image(url)
image.save("sample/david.png")

prompt = "turn him into a cyborg"
images = pipe(prompt, image=image, num_inference_steps=10, guidance_scale=1.1, image_guidance_scale=1).images
images[0].save("sample/david_cyborg.png")

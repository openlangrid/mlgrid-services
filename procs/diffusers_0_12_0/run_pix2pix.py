import PIL
import requests
import torch
from diffusers import StableDiffusionInstructPix2PixPipeline, EulerAncestralDiscreteScheduler

model_id = "timbrooks/instruct-pix2pix"
pipe = StableDiffusionInstructPix2PixPipeline.from_pretrained(
    model_id, torch_dtype=torch.float16, safety_checker=None
).to("cuda")
pipe.scheduler = EulerAncestralDiscreteScheduler.from_config(pipe.scheduler.config)

url = "https://raw.githubusercontent.com/timothybrooks/instruct-pix2pix/main/imgs/example.jpg"
filename = "sample/david.png"

def download_image(url, filename):
    import os
    if os.path.exists(filename):
        image = PIL.Image.open(filename)
        return image
    image = PIL.Image.open(requests.get(url, stream=True).raw)
    image = PIL.ImageOps.exif_transpose(image)
    image = image.convert("RGB")
    image.save(filename)
    return image


image = download_image(url, filename)

prompt = "He wears a trench coat"
#prompt = "turn him into ghibli style"
#prompt = "turn him into a cyborg"
#images = pipe(prompt, image=image, num_inference_steps=10, guidance_scale=1.1, image_guidance_scale=1).images
images = pipe(prompt, image=image).images
images[0].save("sample/david_coat.png")

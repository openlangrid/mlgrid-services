from diffusers import AutoPipelineForImage2Image
from diffusers.utils import load_image
import torch

pipe = AutoPipelineForImage2Image.from_pretrained(
    "stabilityai/sdxl-turbo",
    torch_dtype=torch.float16,
    variant="fp16",
    device_map="auto")

init_image = load_image(
    "./sample/sdxl-turbo_ti2i_input.png"
    ).resize((512, 512))

prompt = "cat wizard, gandalf, lord of the rings, detailed, fantasy, cute, adorable, Pixar, Disney, 8k"

image = pipe(
    prompt,
    image=init_image,
    num_inference_steps=2,
    strength=0.5,
    guidance_scale=0.0
    ).images[0]

image.save("sample/sdxl-turbo_ti2i_output.png")


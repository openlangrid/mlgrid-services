import torch
from diffusers import AutoPipelineForText2Image

device = "cuda"
dtype = torch.float16

pipeline =  AutoPipelineForText2Image.from_pretrained(
    "warp-diffusion/wuerstchen", torch_dtype=dtype
).to(device)

caption = "Anthropomorphic cat dressed as a fire fighter"

output = pipeline(
    prompt=caption,
    height=1024,
    width=1024,
    prior_guidance_scale=4.0,
    decoder_guidance_scale=0.0,
).images

output[0].save("sample/wuerstchen_output.png")

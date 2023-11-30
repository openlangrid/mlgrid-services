from diffusers import AutoPipelineForText2Image
import torch

# パイプラインの準備
pipe = AutoPipelineForText2Image.from_pretrained(
    "stabilityai/sdxl-turbo", 
    torch_dtype=torch.float16, 
    variant="fp16",
    device_map="auto"
)

prompt = "cute cat ear maid"

# 画像生成
image = pipe(
    prompt=prompt, 
    num_inference_steps=1, 
    guidance_scale=0.0
).images[0]

image.save("sample/sdxl-turbo_output.png")


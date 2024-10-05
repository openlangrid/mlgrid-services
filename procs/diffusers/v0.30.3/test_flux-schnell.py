import torch
from diffusers import FluxPipeline

pipe = FluxPipeline.from_pretrained(
   "black-forest-labs/FLUX.1-schnell",
   torch_dtype=torch.bfloat16)
pipe.enable_model_cpu_offload() #save some VRAM by offloading the model to CPU. Remove this if you have enough GPU power

prompt = "A cat holding a sign that says hello world"
image = pipe(
    prompt,
    guidance_scale=0.0,
    output_type="pil",
    num_inference_steps=4,
    max_sequence_length=256,
    generator=torch.Generator("cpu").manual_seed(0)
).images[0]

import os
for n in range(1000):
    fn = f"sample/flux-schnell{n+1}.png"
    if not os.path.exists(fn):
        image.save(fn)
        break

from gpuinfo import get_gpu_properties
import json
props = get_gpu_properties()
print(f"ok {json.dumps(props)}", flush=True)

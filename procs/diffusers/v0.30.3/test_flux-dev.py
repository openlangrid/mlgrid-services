import torch
from diffusers import FluxPipeline

pipe = FluxPipeline.from_pretrained("black-forest-labs/FLUX.1-dev", torch_dtype=torch.bfloat16)
pipe.enable_model_cpu_offload() #save some VRAM by offloading the model to CPU. Remove this if you have enough GPU power

#prompt = "A cat holding a sign that says hello world"
#prompt = "An astronaut riding a horse on Mars."
i = 0
for i in range(2):
 prompt = "A cinematic image capturing a Japanese woman with long black hair, performing a dramatic dive from a helicopter into the vast open sky. The background features a breathtaking view of the sky filled with soaring birds, accentuating a sense of freedom and exhilaration. The woman's expression is focused and fearless, her hair flowing dramatically behind her as she dives. The helicopter is visible in the upper part of the frame, adding a touch of adventure and scale to the scene. The lighting is dynamic, highlighting the action and the expansive atmosphere."
 image = pipe(
    prompt,
    height=1024,
    width=1024,
    guidance_scale=3.5,
    output_type="pil",
    num_inference_steps=50,
    max_sequence_length=512,
    #generator=torch.Generator("cpu").manual_seed(0)
 ).images[0]

 import os
 n = 1
 while True:
    fn = f"sample/flux-dev{n}.png"
    if not os.path.exists(fn):
        break
    n += 1
 image.save(fn)
#26222

from gpuinfo import get_gpu_properties
import json
props = get_gpu_properties()
print(f"ok {json.dumps(props)}", flush=True)



import torch
from diffusers import AnimateDiffPipeline, MotionAdapter, EulerAncestralDiscreteScheduler
from diffusers.utils import export_to_gif

adapter = MotionAdapter.from_pretrained("guoyww/animatediff-motion-adapter-v1-5-2", torch_dtype=torch.float16)
pipe = AnimateDiffPipeline.from_pretrained("SG161222/Realistic_Vision_V6.0_B1_noVAE", motion_adapter=adapter, torch_dtype=torch.float16)
pipe.scheduler = EulerAncestralDiscreteScheduler(
    beta_schedule="linear",
    beta_start=0.00085,
    beta_end=0.012,
)

pipe.enable_free_noise()
pipe.vae.enable_slicing()

pipe.enable_model_cpu_offload()
frames = pipe(
    "An astronaut riding a horse on Mars.",
    num_frames=64,
    num_inference_steps=20,
    guidance_scale=7.0,
    decode_chunk_size=2,
).frames[0]

export_to_gif(frames, "sample/animatediff-freenoise.gif")


from gpuinfo import get_gpu_properties
import json
props = get_gpu_properties()
print(f"ok {json.dumps(props)}", flush=True)

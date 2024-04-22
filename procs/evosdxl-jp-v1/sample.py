from evosdxl_jp_v1 import load_evosdxl_jp
 
prompt = "柴犬"
pipe = load_evosdxl_jp(device="cuda")
images = pipe(prompt, num_inference_steps=4, guidance_scale=0).images
images[0].save("sample/output.png")

from gpuinfo import get_gpu_properties
import json
props = get_gpu_properties()
print(f"ok {json.dumps(props)}", flush=True)

import torch
from diffusers import AutoencoderTiny, StableDiffusionPipeline

from streamdiffusion import StreamDiffusion
from streamdiffusion.image_utils import postprocess_image

# You can load any models using diffuser's StableDiffusionPipeline
pipe = StableDiffusionPipeline.from_pretrained(
    #"stabilityai/japanese-stable-diffusion-xl",  # doesn't work
    #trust_remote_code=True,
    #"runwayml/stable-diffusion-v1-5", # works
    "stabilityai/stable-diffusion-2-1", # doesn't work
    #torch_dtype=torch.float16, use_safetensors=True, variant="fp16",
    "KBlueLeaf/kohaku-v2.1",
    ).to(
    device=torch.device("cuda"),
    dtype=torch.float16,
)

# Wrap the pipeline in StreamDiffusion
# Requires more long steps (len(t_index_list)) in text2image
# You recommend to use cfg_type="none" when text2image
stream = StreamDiffusion(
    pipe,
    t_index_list=[0, 16, 32, 45],
    torch_dtype=torch.float16,
    cfg_type="none",
)

# If the loaded model is not LCM, merge LCM
stream.load_lcm_lora()
stream.fuse_lora()
# Use Tiny VAE for further acceleration
stream.vae = AutoencoderTiny.from_pretrained("madebyollin/taesd").to(device=pipe.device, dtype=pipe.dtype)
# Enable acceleration
#from streamdiffusion.acceleration.tensorrt import accelerate_with_tensorrt
#stream = accelerate_with_tensorrt(
#    stream, "engines", max_batch_size=2,
#)
pipe.enable_xformers_memory_efficient_attention()


prompt = "1girl with dog hair, thick frame glasses"
#prompt = "一人の少女, 犬のような髪, フレームの太いメガネ"
# Prepare the stream
stream.prepare(prompt)

# Warmup >= len(t_index_list) x frame_buffer_size
for _ in range(4):
    stream()

x_output = stream.txt2img()
img = postprocess_image(x_output, output_type="pil")[0]
img.save("out.jpg")

from lmdeploy import pipeline, TurbomindEngineConfig, ChatTemplateConfig
from lmdeploy.vl import load_image
from lmdeploy.vl.utils import encode_image_base64

model = 'OpenGVLab/InternVL3-8B'

#prompt = 'describe this image'
prompt = "明るく親しげな口調で、具体的に絵を褒めて、上達のアドバイスをしてください。"
#image_url = 'https://raw.githubusercontent.com/open-mmlab/mmdeploy/main/tests/data/tiger.jpeg'
image_url = "./dora.png"
image = load_image(image_url)
pipe = pipeline(model,
                backend_config=TurbomindEngineConfig(session_len=16384, tp=1, cache_max_entry_count=0.2),
                chat_template_config=ChatTemplateConfig(model_name='internvl2_5'))
response = pipe([
    {"role": "system", "content": "あなたは絵のアドバイザーです。"},
    {"role": "user", "content": [
        {"type": "text", "text": prompt},
        {"type": "image_url", "image_url": {"url": f"data:image/jpeg;base64,{encode_image_base64(image)}"}}
    ]}
])
print(response.text)

from gpuinfo import get_gpu_properties
import json
props = get_gpu_properties()
print(f"ok {json.dumps(props)}", flush=True)

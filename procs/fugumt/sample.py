from transformers import pipeline
import torch
trans = pipeline('translation', model='staka/fugumt-ja-en',
                 device=0)
trans('猫はかわいいです。')

from gpuinfo import get_gpu_properties
import json
props = get_gpu_properties()
print(f"ok {json.dumps(props)}", flush=True)

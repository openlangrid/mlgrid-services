import yaml, json
import torch

def convDict(value):
    return dict(map(convItem, value.items()))

def convItem(item):
    [key, value] = item
    if isinstance(value, dict):
        return (key, convDict(value))
    if key == "torch_dtype":
        return (key, eval(value))
    return (key, value)

with open('defaults.yml', 'r') as yml:
    config = yaml.safe_load(yml)
    config = convDict(config)
    print(config)

print(eval("torch.float16"))

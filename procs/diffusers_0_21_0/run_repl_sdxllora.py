def run(model_name: str, lora_model_name: str):
    import torch
    from diffusers import DiffusionPipeline

    base = DiffusionPipeline.from_pretrained(
        model_name,
        torch_dtype=torch.float16, variant="fp16", use_safetensors=True,
        device_map="auto")
    if lora_model_name:
        base.load_lora_weights(
            lora_model_name,
            low_cpu_mem_usage=True)

    print("ready", flush=True)

    import json, sys
    for line in sys.stdin:
        input = json.loads(line)
        with open(input["promptPath"]) as f:
            prompt = f.read()
        language = input["promptLanguage"]
        numberOfTimes = input["numberOfTimes"]
        outputPathPrefix = input["outputPathPrefix"]

        for i in range(numberOfTimes):
            image = base(
                prompt=prompt
                ).images[0]
            image.save(f"{outputPathPrefix}_{i}.png")

        print("ok", flush=True)


def main(model: str, loraModel: str):
    run(model, loraModel)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?",
        default="stabilityai/stable-diffusion-xl-base-1.0")
    parser.add_argument("--loraModel", type=str, nargs="?",
        default=None)
    main(**vars(parser.parse_args()))

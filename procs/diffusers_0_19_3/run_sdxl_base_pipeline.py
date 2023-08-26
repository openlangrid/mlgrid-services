def run(model_name: str):
    import torch
    from diffusers import DiffusionPipeline

    base = DiffusionPipeline.from_pretrained(
        model_name,
        torch_dtype=torch.float16, variant="fp16", use_safetensors=True)
    base.to("cuda")

    print("ready", flush=True)

    import json, sys
    for line in sys.stdin:
        input = json.loads(line)
        with open(input["promptPath"]) as f:
            prompt = f.read()
        language = input["promptLanguage"]
        numberOfTimes = input["numberOfTimes"]
        outputPathPrefix = input["outputPathPrefix"]

        retImages = []
        for i in range(numberOfTimes):
            image = base(
                prompt=prompt
                ).images[0]
            retImages.append(image)
            image.save(f"{outputPathPrefix}_{i}.png")

        print("ok", flush=True)


def main(model: str):
    run(model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--model", type=str, default="stabilityai/stable-diffusion-xl-base-1.0")
    main(**vars(parser.parse_args()))

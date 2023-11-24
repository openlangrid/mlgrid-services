def run(model_name: str):
    from diffusers import DiffusionPipeline
    import torch

    pipeline = DiffusionPipeline.from_pretrained(
        model_name, trust_remote_code=True
    )
    pipeline.to("cuda")

#    base = DiffusionPipeline.from_pretrained(
#        model_name,
#        torch_dtype=torch.float16, variant="fp16", use_safetensors=True,
 #       device_map="auto")

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
            image = pipeline(
                prompt=prompt
                ).images[0]
            image.save(f"{outputPathPrefix}_{i}.png")

        print("ok", flush=True)


def main(model: str):
    run(model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?",
        default="stabilityai/japanese-stable-diffusion-xl")
    main(**vars(parser.parse_args()))

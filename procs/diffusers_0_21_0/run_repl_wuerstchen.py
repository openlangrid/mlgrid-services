def run(model_name: str):
    import torch
    from diffusers import AutoPipelineForText2Image
    pipeline =  AutoPipelineForText2Image.from_pretrained(
        model_name,
        torch_dtype=torch.float16,
        device_map="auto"
    )
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
                prompt=prompt,
                height=1024,
                width=1024,
                prior_guidance_scale=4.0,
                decoder_guidance_scale=0.0,
            ).images[0]
            image.save(f"{outputPathPrefix}_{i}.png")
        print("ok", flush=True)


def main(model: str):
    run(model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?",
        default="warp-diffusion/wuerstchen")
    main(**vars(parser.parse_args()))

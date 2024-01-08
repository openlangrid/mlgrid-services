def run(model_name: str):
    from diffusers import AutoPipelineForText2Image
    import torch
    pipe = AutoPipelineForText2Image.from_pretrained(
        model_name, 
        torch_dtype=torch.float16, 
        variant="fp16",
        device_map="auto")
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
            image = pipe(
                prompt=prompt, 
                num_inference_steps=1, 
                guidance_scale=0.0
                ).images[0]
            image.save(f"{outputPathPrefix}_{i}.png")
        from gpuinfo import get_gpu_properties
        props = get_gpu_properties()
        print(f"ok {json.dumps(props)}", flush=True)


def main(model: str):
    run(model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?",
        default="stabilityai/sdxl-turbo")
    main(**vars(parser.parse_args()))

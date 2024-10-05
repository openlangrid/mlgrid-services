def run(model_name: str):
    import torch
    from diffusers import FluxPipeline
    pipe = FluxPipeline.from_pretrained(
      model_name,
      torch_dtype=torch.bfloat16)
    pipe.enable_model_cpu_offload()
    print("ready", flush=True)

    params_dev = {
        "guidance_scale": 3.5,
        "num_inference_steps": 50,
        "max_sequence_length": 512,
    }
    params_schnell = {
        "guidance_scale": 0.0,
        "num_inference_steps": 4,
        "max_sequence_length": 512,
    }
    params = params_dev if model_name == "black-forest-labs/FLUX.1-dev" else params_schnell
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
                prompt,
                output_type="pil",
#                generator=torch.Generator("cpu").manual_seed(0),
                **params
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
        default="black-forest-labs/FLUX.1-dev")
    main(**vars(parser.parse_args()))

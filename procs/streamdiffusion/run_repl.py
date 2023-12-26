def run(model_name: str):
    import torch
    from diffusers import AutoencoderTiny, StableDiffusionPipeline
    from streamdiffusion import StreamDiffusion
    from streamdiffusion.image_utils import postprocess_image
     # You can load any models using diffuser's StableDiffusionPipeline
    pipe = StableDiffusionPipeline.from_pretrained(model_name).to(
        device=torch.device("cuda"),
        dtype=torch.float16,
    )
    stream = StreamDiffusion(
        pipe,
        t_index_list=[0, 16, 32, 45],
        torch_dtype=torch.float16,
        cfg_type="none",
    )
    stream.load_lcm_lora()
    stream.fuse_lora()
    stream.vae = AutoencoderTiny.from_pretrained("madebyollin/taesd").to(device=pipe.device, dtype=pipe.dtype)
    pipe.enable_xformers_memory_efficient_attention()
    print("ready", flush=True)

    import json, sys
    for line in sys.stdin:
        input = json.loads(line)
        with open(input["promptPath"]) as f:
            prompt = f.read()
        language = input["promptLanguage"]
        numberOfTimes = input["numberOfTimes"]
        outputPathPrefix = input["outputPathPrefix"]

        stream.prepare(prompt)
        # Warmup >= len(t_index_list) x frame_buffer_size
        for _ in range(4):
            stream()
        for i in range(numberOfTimes):
            x_output = stream.txt2img()
            image = postprocess_image(x_output, output_type="pil")[0]
            image.save(f"{outputPathPrefix}_{i}.png")
        print("ok", flush=True)


def main(model: str):
    run(model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?",
        default="KBlueLeaf/kohaku-v2.1")
    main(**vars(parser.parse_args()))

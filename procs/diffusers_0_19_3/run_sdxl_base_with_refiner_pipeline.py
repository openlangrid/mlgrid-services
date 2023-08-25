def run(model_name: str, refiner_model_name: str):
    import torch
    from diffusers import DiffusionPipeline

    base = DiffusionPipeline.from_pretrained(
        model_name,
        torch_dtype=torch.float16, variant="fp16", use_safetensors=True)
    base.to("cuda")
    refiner = DiffusionPipeline.from_pretrained(
        refiner_model_name,
        torch_dtype=torch.float16, variant="fp16", use_safetensors=True,
        text_encoder_2=base.text_encoder_2, vae=base.vae)
    refiner.to("cuda")

    n_steps = 40
    high_noise_frac = 0.8

    print("ready", flush=True)

    import json, sys
    for line in sys.stdin:
        if(line.strip() == "terminate"):
            break
        input = json.loads(line)
        promptPath = input["promptPath"]
        language = input["language"]
        numberOfTimes = input["numberOfTimes"]
        outputPathPrefix = input["outputPathPrefix"]

        with open(promptPath) as f:
            prompt = f.read()

        retImages = []
        for i in range(numberOfTimes):
            images = base(
                prompt=prompt,
                num_inference_steps=n_steps,
                denoising_end=high_noise_frac,
                output_type="latent"
                ).images
            images = refiner(
                prompt=prompt,
                image=images,
                num_inference_steps=n_steps,
                denoising_start=high_noise_frac
                ).images
            retImages.append(images[0])

        # save images
        for i in range(len(retImages)):
            image = retImages[i]
            image.save(f"{outputPathPrefix}_{i}.png")
        print("ok", flush=True)


def main(model: str, refinerModel: str):
    run(model, refinerModel)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--model", type=str, default="stabilityai/stable-diffusion-xl-base-1.0")
    parser.add_argument("--refinerModel", type=str, default="stabilityai/stable-diffusion-xl-refiner-1.0")
    main(**vars(parser.parse_args()))

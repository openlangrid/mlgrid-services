def run():
    from evosdxl_jp_v1 import load_evosdxl_jp
    prompt = "柴犬"
    pipe = load_evosdxl_jp(device="cuda")
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
            image = pipe(prompt,
                num_inference_steps=4,
                guidance_scale=0).images[0]
            image.save(f"{outputPathPrefix}_{i}.png")
        from gpuinfo import get_gpu_properties
        props = get_gpu_properties()
        print(f"ok {json.dumps(props)}", flush=True)


def main():
    run()


if __name__ == "__main__": 
    main()

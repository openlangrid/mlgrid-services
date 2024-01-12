def run(model_name: str):
#    from torch.cuda import OutOfMemoryError
    try:
        from transformers import pipeline
        trans = pipeline('translation', model=model_name)
        print("ready", flush=True)

        import json, sys, torch
        from gpuinfo import get_gpu_properties
        for line in sys.stdin:
            input = json.loads(line)
            with open(input["textPath"]) as f:
                text = f.read()
            textLanguage = input["textLanguage"]
            targetLanguage = input["targetLanguage"]
            outputPath = input["outputPath"]
            with torch.no_grad():
                result = trans(text)[0]["translation_text"]
            with open(f"{outputPath}", 'w', encoding='UTF-8') as f:
                f.write(result)
            props = get_gpu_properties()
            print(f"ok {json.dumps(props)}", flush=True)

#    except OutOfMemoryError:
#        print("ng torch.cuda.OutOfMemoryError", flush=True)
    except RuntimeError:  # RuntimeError: "addmm_impl_cpu_" not implemented for 'Half'
        print("ng RuntimeError", flush=True)


def main(model: str):
    run(model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?", default="staka/fugumt-ja-en")
    main(**vars(parser.parse_args()))

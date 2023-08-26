def run(tokenizer_model_name: str, model_name: str):
    from transformers import AutoModelForCausalLM, AutoTokenizer
    from transformers.generation import GenerationConfig

    tokenizer = AutoTokenizer.from_pretrained(
        tokenizer_model_name, trust_remote_code=True)

    # use fp16
    # model = AutoModelForCausalLM.from_pretrained("Qwen/Qwen-7B", device_map="auto", trust_remote_code=True, fp16=True).eval()
    # use auto mode, automatically select precision based on the device.
    model = AutoModelForCausalLM.from_pretrained(
        model_name, device_map="auto",
        trust_remote_code=True).eval()

    model.generation_config = GenerationConfig.from_pretrained(
        model_name, trust_remote_code=True)

    print("ready", flush=True)

    import json, sys
    for line in sys.stdin:
        input = json.loads(line)
        with open(input["textPath"]) as f:
            text = f.read()
        language = input["textLanguage"]
        outputPath = input["outputPath"]

        inputs = tokenizer(text, return_tensors='pt')
        inputs = inputs.to(model.device)
        pred = model.generate(**inputs)
        ret = tokenizer.decode(pred.cpu()[0], skip_special_tokens=True)
        with open(outputPath, mode="w") as f:
            f.write(ret)

        print("ok", flush=True)


def main(tokenizerModel: str, model: str):
    run(tokenizerModel if tokenizerModel is not None else model,
        model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?", default="Qwen/Qwen-7B")
    parser.add_argument("--tokenizerModel", type=str, default=None)
    main(**vars(parser.parse_args()))

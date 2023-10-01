
def run(tokenizer_model_name: str, model_name: str):
    from transformers import AutoModelForCausalLM, AutoTokenizer
    import torch

    # トークナイザーとモデルの準備
    tokenizer = AutoTokenizer.from_pretrained(
        tokenizer_model_name,
#        use_fast=True
    )
    model = AutoModelForCausalLM.from_pretrained(
        model_name,
        device_map="auto"
    )
    model.eval()

    print("ready", flush=True)

    import json
    import sys
    for line in sys.stdin:
        input = json.loads(line)
        with open(input["textPath"]) as f:
            text = f.read()
        language = input["textLanguage"]
        outputPath = input["outputPath"]

        input_ids = tokenizer(text, return_tensors="pt").input_ids
        input_ids = input_ids.to(model.device)
        samples = model.generate(
            inputs=input_ids, max_new_tokens=512, temperature=0.7)
        output = tokenizer.decode(samples[0][input_ids.shape[1]:], skip_special_tokens=True)
        with open(outputPath, mode="w") as f:
            f.write(str(output))

        print("ok", flush=True)


def main(tokenizerModel: str, model: str):
    run(tokenizerModel if tokenizerModel is not None else model
        , model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?", default="Xwin-LM/Xwin-LM-7B-V0.1")
    parser.add_argument("--tokenizerModel", type=str, default=None)
    main(**vars(parser.parse_args()))

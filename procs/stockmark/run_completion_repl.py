
def run(tokenizer_model_name: str, model_name: str):
    import torch
    from transformers import AutoModelForCausalLM, AutoTokenizer
    tokenizer = AutoTokenizer.from_pretrained(
        tokenizer_model_name)
    model = AutoModelForCausalLM.from_pretrained(
        "stockmark/stockmark-13b",
        device_map="auto",
        torch_dtype=torch.bfloat16)
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
        inputs = tokenizer("自然言語処理とは", return_tensors="pt").to(model.device)
        with torch.no_grad():
            tokens = model.generate(
                **inputs,
                max_new_tokens=1024,
                do_sample=True,
                temperature=0.7
            )[0]
        input_ids = inputs.input_ids
        generated_text = tokenizer.decode(tokens[input_ids.shape[1]:])
        with open(outputPath, mode="w") as f:
            f.write(str(generated_text))
        print("ok", flush=True)


def main(tokenizerModel: str, model: str):
    run(tokenizerModel if tokenizerModel is not None else model
        , model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?", default="stockmark/stockmark-13b")
    parser.add_argument("--tokenizerModel", type=str, default=None)
    main(**vars(parser.parse_args()))

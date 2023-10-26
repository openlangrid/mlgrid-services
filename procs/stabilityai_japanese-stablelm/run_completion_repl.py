
def run(tokenizer_model_name: str, model_name: str):
    from transformers import AutoTokenizer, AutoModelForCausalLM
    tokenizer = AutoTokenizer.from_pretrained(
        tokenizer_model_name)
    model = AutoModelForCausalLM.from_pretrained(
        model_name,
        torch_dtype="auto",
        device_map="auto")
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

        input_ids = tokenizer(text, return_tensors="pt")
        input_ids = input_ids.to(model.device)
        generated_tokens = model.generate(
            **input_ids,
            max_new_tokens=512,
            temperature=0.75,
            top_p=0.95,
            do_sample=True)[0]
        input_ids = input_ids.input_ids
        generated_text = tokenizer.decode(generated_tokens[input_ids.shape[1]:])
        with open(outputPath, mode="w") as f:
            f.write(str(generated_text))
        print("ok", flush=True)


def main(tokenizerModel: str, model: str):
    run(tokenizerModel if tokenizerModel is not None else model
        , model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?", default="stabilityai/japanese-stablelm-base-gamma-7b")
    parser.add_argument("--tokenizerModel", type=str, default=None)
    main(**vars(parser.parse_args()))


def run(tokenizer_model_name: str, model_name: str):
    from transformers import AutoTokenizer, AutoModelForCausalLM

    tokenizer = AutoTokenizer.from_pretrained(
        tokenizer_model_name,
        trust_remote_code=True)
    model = AutoModelForCausalLM.from_pretrained(
        model_name,
        device_map="auto",
        trust_remote_code=True)
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
        generated_tokens = model.generate(
            inputs=input_ids,
            max_new_tokens=32,
            do_sample=True,
            top_k=50,
            top_p=0.95,
            temperature=1.0,
        )[0]
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
    parser.add_argument("model", type=str, nargs="?", default="pfnet/plamo-13b")
    parser.add_argument("--tokenizerModel", type=str, default=None)
    main(**vars(parser.parse_args()))

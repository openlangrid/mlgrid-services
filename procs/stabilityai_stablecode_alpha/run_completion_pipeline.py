
def run(tokenizer_model_name: str, model_name: str):
    import json
    from transformers import AutoModelForCausalLM, AutoTokenizer

    tokenizer = AutoTokenizer.from_pretrained(tokenizer_model_name)
    model = AutoModelForCausalLM.from_pretrained(model_name,
        trust_remote_code=True,
        torch_dtype="auto",
    )
    model.cuda()

    print("ready", flush=True)

    import sys
    for line in sys.stdin:
        input = json.loads(line)
        with open(input["textPath"]) as f:
            text = f.read()
        language = input["textLanguage"]
        outputPath = input["outputPath"]

        inputs = tokenizer(
            text,
            return_tensors="pt",
            return_token_type_ids=False).to("cuda")
        tokens = model.generate(
            **inputs,
            max_new_tokens=512,
            temperature=0.2,
            do_sample=True)
        ret = tokenizer.decode(
            tokens[0],
            skip_special_tokens=True)
        with open(outputPath, mode="w") as f:
            f.write(str(ret))

        print("ok", flush=True)


def main(tokenizerModel: str, model: str):
    run(tokenizerModel if tokenizerModel is not None else model
        , model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--tokenizerModel", type=str, default=None)
    parser.add_argument("--model", type=str, default="stabilityai/stablecode-completion-alpha-3b")
    main(**vars(parser.parse_args()))

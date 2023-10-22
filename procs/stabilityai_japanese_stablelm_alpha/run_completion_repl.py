def run(tokenizer_model_name: str, model_name: str):
    import torch
    from transformers import LlamaTokenizer, AutoModelForCausalLM
    tokenizer = LlamaTokenizer.from_pretrained(
        tokenizer_model_name,
        additional_special_tokens=['▁▁'])
    model = AutoModelForCausalLM.from_pretrained(
        model_name,
        trust_remote_code=True,
        torch_dtype=torch.float16,
        device_map="auto")
    model.eval()
    print("ready", flush=True)

    import json, sys
    for line in sys.stdin:
        input = json.loads(line)
        with open(input["textPath"]) as f:
            text = f.read()
        language = input["textLanguage"]
        outputPath = input["outputPath"]
        input_ids = tokenizer.encode(
            text,
            add_special_tokens=False,
            return_tensors="pt"
        )
        input_ids = input_ids.to(model.device)
        tokens = model.generate(
            input_ids,
            max_new_tokens=512,
            temperature=0.7,
            top_p=0.95,
            do_sample=True,
        )[0]
        tokens = tokens[input_ids.size(1):]
        output = tokenizer.decode(tokens, skip_special_tokens=True)
#            .strip().rstrip("<|endoftext|>")
        with open(outputPath, mode="w") as f:
            f.write(output)
        print("ok", flush=True)


def main(tokenizerModel: str, model: str):
    run(tokenizerModel if tokenizerModel is not None else model
        , model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?", default="stabilityai/japanese-stablelm-base-alpha-7b")
    parser.add_argument("--tokenizerModel", type=str, default="novelai/nerdstash-tokenizer-v1")
    main(**vars(parser.parse_args()))


def run(tokenizer_model_name: str, model_name: str):
    import torch
    from transformers import AutoModelForCausalLM, AutoTokenizer

    tokenizer = AutoTokenizer.from_pretrained(
        tokenizer_model_name,
        use_fast=False)
    model = AutoModelForCausalLM.from_pretrained(
        model_name,
        torch_dtype=torch.float16,
        low_cpu_mem_usage=True,
        device_map="auto")

    print("ready", flush=True)

    import json, sys
    for line in sys.stdin:
        input = json.loads(line)
        with open(input["textPath"]) as f:
            text = f.read()
        language = input["textLanguage"]
        outputPath = input["outputPath"]

#        system_prompt = "### System:\nYou are StableBeluga, an AI that follows instructions extremely well. Help as much as you can. Remember, be safe, and don't do anything illegal.\n\n"
#        message = "Write me a poem please"
#        prompt = f"{system_prompt}### User: {message}\n\n### Assistant:\n"
        inputs = tokenizer(
            text,
            return_tensors="pt").to("cuda")
        output = model.generate(
            **inputs,
            do_sample=True,
            top_p=0.95,
            top_k=0,
            max_new_tokens=256)
        ret = tokenizer.decode(
            output[0],
            skip_special_tokens=True)

#        ret = ret.rstrip("<|endoftext|>")
        with open(outputPath, mode="w") as f:
            f.write(ret)

        print("ok", flush=True)


def main(tokenizerModel: str, model: str):
    run(tokenizerModel if tokenizerModel is not None else model
        , model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?", default="stabilityai/StableBeluga-7B")
    parser.add_argument("--tokenizerModel", type=str, default=None)
    main(**vars(parser.parse_args()))

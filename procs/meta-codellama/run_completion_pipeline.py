
def run(tokenizer_model_name: str, model_name: str):
    import json
    from transformers import AutoModelForCausalLM, AutoTokenizer
    import torch

    tokenizer = AutoTokenizer.from_pretrained(
        tokenizer_model_name
    )
    model = AutoModelForCausalLM.from_pretrained(
        model_name,
        torch_dtype=torch.float16,
        device_map="auto",
    )
    print("ready", flush=True)

    import sys
    for line in sys.stdin:
        input = json.loads(line)
        with open(input["textPath"]) as f:
            text = f.read()
        language = input["textLanguage"]
        outputPath = input["outputPath"]

        inputs = tokenizer(text, return_tensors="pt").to("cuda")
        outputs = model.generate(
            inputs["input_ids"],
            do_sample=True,
            temperature=0.2,
            top_p=0.9,
            eos_token_id=tokenizer.eos_token_id,
            max_length=1024,
        )
        output = tokenizer.decode(
            outputs[0].to("cpu")
            ).lstrip("<s>")
        with open(outputPath, mode="w") as f:
            f.write(output)

        print("ok", flush=True)


def main(tokenizerModel: str, model: str):
    run(tokenizerModel if tokenizerModel is not None else model
        , model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?", default="codellama/CodeLlama-7b-hf")
    parser.add_argument("--tokenizerModel", type=str, default=None)
    main(**vars(parser.parse_args()))

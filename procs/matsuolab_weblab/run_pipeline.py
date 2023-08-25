
def run(tokenizer_model_name: str, model_name: str):
    import json
    import torch
    from transformers import AutoTokenizer, AutoModelForCausalLM

    tokenizer = AutoTokenizer.from_pretrained(tokenizer_model_name)
    model = AutoModelForCausalLM.from_pretrained(model_name)

    if torch.cuda.is_available():
        model = model.to("cuda")

    print("ready", flush=True)

    import sys
    for line in sys.stdin:
        if(line.strip() == "terminate"):
            break
        input = json.loads(line)
        inputPath = input["inputPath"]
        inputLanguage = input["inputLanguage"]
        outputPath = input["outputPath"]
        with open(inputPath) as f:
            text = f.read()
        if model_name.endswith("-16k"):
            generate_args = {"max_new_tokens": 1024 * 4}
        else:
            generate_args = {}

        token_ids = tokenizer.encode(text, add_special_tokens=False, return_tensors="pt")
        with torch.no_grad():
            output_ids = model.generate(
                token_ids.to(model.device),
                max_new_tokens=256,
                do_sample=True,
                temperature=0.7,
                top_p=0.95,
                #pad_token_id=tokenizer.pad_token_id,
                #bos_token_id=tokenizer.bos_token_id,
                #eos_token_id=tokenizer.eos_token_id
                )

        ret = tokenizer.decode(output_ids.tolist()[0])
        ret = ret.rstrip("<|endoftext|>")
        with open(outputPath, mode="w") as f:
            f.write(str(ret))
        print("ok", flush=True)


def main(tokenizer_model: str, model: str):
    run(tokenizer_model if tokenizer_model is not None else model
        , model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--tokenizer_model", type=str, default=None)
    parser.add_argument("--model", type=str, default="matsuo-lab/weblab-10b")
    main(**vars(parser.parse_args()))

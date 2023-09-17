
def run(tokenizer_model_name: str, model_name: str):
    import torch
    from transformers import AutoProcessor
    from heron.models.git_llm.git_llama import GitLlamaForCausalLM
    model = GitLlamaForCausalLM.from_pretrained(
        model_name, torch_dtype=torch.float16
    )
    model.eval()
    model.to("cuda:0")
    processor = AutoProcessor.from_pretrained(model_name)
    print("ready", flush=True)

    from PIL import Image
    import json, re, sys
    p = re.compile("<s> ##human: .*\n##gpt: (.*)\n")
    for line in sys.stdin:
        input = json.loads(line)
        with open(input["promptPath"]) as f:
            prompt = f.read()
        language = input["promptLanguage"]
        image = Image.open(input["imagePath"])
        outputPath = input["outputPath"]

        # do preprocessing
        prompt = f"##human: {prompt}\n##gpt: "
        inputs = processor(
            prompt, image,
            return_tensors="pt",
            truncation=True,
        )
        inputs = {k: v.to("cuda:0") for k, v in inputs.items()}

        # set eos token
        eos_token_id_list = [
            processor.tokenizer.pad_token_id,
            processor.tokenizer.eos_token_id,
        ]

        # do inference
        with torch.no_grad():
            out = model.generate(
                **inputs,
                max_length=256, do_sample=False, temperature=0.,
                eos_token_id=eos_token_id_list)
            out = processor.tokenizer.batch_decode(out)[0]
            print(f"process result:\n{out}", file=sys.stderr)
            m = p.match(out)
            if m:
                out = m.group(1)
            with open(outputPath, mode="w") as f:
                f.write(out)

        print("ok", flush=True)


def main(tokenizerModel: str, model: str):
    run(tokenizerModel if tokenizerModel is not None else model
        , model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?", default="turing-motors/heron-chat-git-Llama-2-7b-v0")
    parser.add_argument("--tokenizerModel", type=str, default=None)
    main(**vars(parser.parse_args()))

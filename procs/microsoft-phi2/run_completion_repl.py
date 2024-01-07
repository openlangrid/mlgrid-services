
def run(tokenizer_model_name: str, model_name: str):
    import torch
    from transformers import AutoModelForCausalLM, AutoTokenizer
    torch.set_default_device("cuda")
    tokenizer = AutoTokenizer.from_pretrained(
        tokenizer_model_name, trust_remote_code=True)
    model = AutoModelForCausalLM.from_pretrained(
        model_name, torch_dtype="auto",
        device_map="auto", trust_remote_code=True)
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
        prompt = text
        inputs = tokenizer(prompt, return_tensors="pt", return_attention_mask=False)
        outputs = model.generate(**inputs, max_length=200)
        inputTokenLen = len(inputs["input_ids"][0])
        generated_text = tokenizer.decode(outputs[0][inputTokenLen:]).split("<|endoftext|>")[0]
        with open(outputPath, mode="w") as f:
            f.write(str(generated_text))
        from gpuinfo import get_gpu_properties
        props = get_gpu_properties()
        print(f"ok {json.dumps(props)}", flush=True)


def main(tokenizerModel: str, model: str):
    run(tokenizerModel if tokenizerModel is not None else model
        , model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?", default="microsoft/phi-2")
    parser.add_argument("--tokenizerModel", type=str, default=None)
    main(**vars(parser.parse_args()))


def run(tokenizer_model_name: str, model_name: str, text: str):
    import torch
    from transformers import AutoModelForCausalLM, AutoTokenizer

    if not tokenizer_model_name:
        tokenizer_model_name = model_name

    # Use torch.bfloat16 for A100 GPU and torch.flaot16 for the older generation GPUs
    torch_dtype = torch.bfloat16 if torch.cuda.is_available() and hasattr(torch.cuda, "is_bf16_supported") and torch.cuda.is_bf16_supported() else torch.float16

    tokenizer = AutoTokenizer.from_pretrained(
        tokenizer_model_name)
    model = AutoModelForCausalLM.from_pretrained(
        model_name,
        device_map="auto",
        torch_dtype=torch_dtype)

    inputs = tokenizer(text, return_tensors="pt").to(model.device)
    with torch.no_grad():
        tokens = model.generate(
            **inputs,
            max_new_tokens=128,
            repetition_penalty=1.1
        )
    return tokenizer.decode(tokens[0], skip_special_tokens=True)

#        ).strip().rstrip("<|endoftext|>")

def main(tokenizer_model: str, model: str, inputPath: str, inputLanguage: str, outputPath: str):
    with open(inputPath) as f:
        text = f.read()
    ret = run(tokenizer_model, model, text)
    with open(outputPath, mode="w") as f:
        f.write(str(ret))


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--tokenizer_model", type=str, default=None)
    parser.add_argument("--model", type=str, default="stockmark/gpt-neox-japanese-1.4b")
    parser.add_argument("--inputPath", type=str, default="./sample/input.txt")
    parser.add_argument("--inputLanguage", type=str, default="ja")
    parser.add_argument("--outputPath", type=str, default="./sample/output.txt")
    main(**vars(parser.parse_args()))

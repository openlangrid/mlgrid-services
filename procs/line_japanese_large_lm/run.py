
def run(tokenizer_model_name: str, model_name: str, text: str):
    import torch
    from transformers import AutoModelForCausalLM, AutoTokenizer, pipeline, set_seed

    if not tokenizer_model_name:
        tokenizer_model_name = model_name

    model = AutoModelForCausalLM.from_pretrained(
        tokenizer_model_name,
        torch_dtype=torch.float16)
    tokenizer = AutoTokenizer.from_pretrained(
        model_name,
        use_fast=False)
    generator = pipeline(
        "text-generation",
        model=model,
        tokenizer=tokenizer, device=0)
#    set_seed(101)
    
    output = generator(
        text,
#        max_length=30,
        temperature=0.7,
        max_new_tokens=256,
        do_sample=True,
        pad_token_id=tokenizer.pad_token_id,
        num_return_sequences=1)

    return output[0]["generated_text"]


def main(tokenizerModel: str, model: str, inputPath: str, inputLanguage: str, outputPath: str):
    with open(inputPath) as f:
        text = f.read()
    ret = run(
        tokenizerModel if tokenizerModel else model,
        model, text)
    with open(outputPath, mode="w") as f:
        f.write(str(ret))


if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--tokenizerModel", type=str, default=None)
    parser.add_argument("--model", type=str, default="line-corporation/japanese-large-lm-1.7b")
    parser.add_argument("--inputPath", type=str, default="./sample/input.txt")
    parser.add_argument("--inputLanguage", type=str, default="ja")
    parser.add_argument("--outputPath", type=str, default="./sample/output.txt")
    main(**vars(parser.parse_args()))

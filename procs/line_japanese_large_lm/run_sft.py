
def run(tokenizer_model_name: str, model_name: str, prompt: str):
    import torch
    from transformers import AutoModelForCausalLM, AutoTokenizer, pipeline

    tokenizer = AutoTokenizer.from_pretrained(tokenizer_model_name, use_fast=False)
    model = AutoModelForCausalLM.from_pretrained(model_name)

    generator = pipeline("text-generation", model=model, tokenizer=tokenizer, device=0)

    out = generator(
        prompt,
        max_length = 256,
        do_sample = True,
        temperature = 0.7,
        top_p = 0.9,
        top_k = 0,
        repetition_penalty = 1.1,
        num_beams = 1,
        pad_token_id = tokenizer.pad_token_id,
        num_return_sequences = 1,
    )
    return out[0]["generated_text"]


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
    parser.add_argument("--model", type=str, default="line-corporation/japanese-large-lm-3.6b-instruction-sft")
    parser.add_argument("--inputPath", type=str, default="./sample/input_sft.txt")
    parser.add_argument("--inputLanguage", type=str, default="ja")
    parser.add_argument("--outputPath", type=str, default="./sample/output_sft.txt")
    main(**vars(parser.parse_args()))

def run(model_name: str, text: str):
    import torch
    from transformers import AutoTokenizer, AutoModelForCausalLM

    tokenizer = AutoTokenizer.from_pretrained(model_name, use_fast=False)
    model = AutoModelForCausalLM.from_pretrained(model_name)

    if torch.cuda.is_available():
        model = model.to("cuda")

    token_ids = tokenizer.encode(text, add_special_tokens=False, return_tensors="pt")

    with torch.no_grad():
        output_ids = model.generate(
            token_ids.to(model.device),
            max_new_tokens=512,
            do_sample=True,
            temperature=1.0,
            top_p=0.85,
            pad_token_id=tokenizer.pad_token_id,
            bos_token_id=tokenizer.bos_token_id,
            eos_token_id=tokenizer.eos_token_id
        )

    return tokenizer.decode(output_ids.tolist()[0][token_ids.size(1):]).rstrip("</s>")


def main(model: str, inputPath: str, inputLanguage: str, outputPath: str):
    print(f"model: {model}")
    with open(inputPath) as f:
        text = f.read()
    ret = run(model, text)
    with open(outputPath, mode="w") as f:
        f.write(str(ret))


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--model", type=str, default="rinna/bilingual-gpt-neox-4b-instruction-sft")
    parser.add_argument("--inputPath", type=str, default="./sample/sft_input.txt")
    parser.add_argument("--inputLanguage", type=str, default="ja")
    parser.add_argument("--outputPath", type=str, default="./sample/sft_output.txt")
    main(**vars(parser.parse_args()))

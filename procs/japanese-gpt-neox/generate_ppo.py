def run(model_name: str, text: str):
    text = text.replace("\n", "<NL>")

    import torch
    from transformers import AutoModelForCausalLM, AutoTokenizer
    tokenizer = AutoTokenizer.from_pretrained(model_name, use_fast=False)
    model = AutoModelForCausalLM.from_pretrained(model_name)
    if torch.cuda.is_available():
        model = model.to("cuda")
    token_ids = tokenizer.encode(text, add_special_tokens=False, return_tensors="pt")
    with torch.no_grad():
        output_ids = model.generate(
            token_ids.to(model.device),
            do_sample=True,
            max_new_tokens=128,
            temperature=0.7,
            repetition_penalty=1.1,
            pad_token_id=tokenizer.pad_token_id,
            bos_token_id=tokenizer.bos_token_id,
            eos_token_id=tokenizer.eos_token_id
        )
    output = tokenizer.decode(output_ids.tolist()[0][token_ids.size(1):])
    output = output.replace("<NL>", "\n")
    return output


def main(model: str, inputPath: str, inputLanguage: str, outputPath: str):
    with open(inputPath) as f:
        text = f.read()
    ret = run(model, text)
    with open(outputPath, mode="w") as f:
        f.write(str(ret))


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--model", type=str, default="rinna/japanese-gpt-neox-3.6b-instruction-ppo")
    parser.add_argument("--inputPath", type=str, default="./sample/input.txt")
    parser.add_argument("--inputLanguage", type=str, default="ja")
    parser.add_argument("--outputPath", type=str, default="./sample/output.txt")
    main(**vars(parser.parse_args()))

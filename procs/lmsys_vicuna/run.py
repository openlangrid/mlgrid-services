
def run(tokenizer_model_name: str, model_name: str, text: str, generate_args: dict = {}):
    import torch
    from transformers import AutoTokenizer, AutoModelForCausalLM

    if not tokenizer_model_name:
        tokenizer_model_name = model_name

    # トークナイザーとモデルの準備
    tokenizer = AutoTokenizer.from_pretrained(
        tokenizer_model_name,
        use_fast=False,
    )
    model = AutoModelForCausalLM.from_pretrained(
        model_name,
#        load_in_8bit=True,
        torch_dtype=torch.float16,
        device_map="auto",
    )

    token_ids = tokenizer.encode(text, add_special_tokens=False, return_tensors="pt")
    with torch.no_grad():
        args = {
            "max_new_tokens": 1024,
            "do_sample": True,
            "temperature": 0.7,
            "top_p": 0.85,
            "pad_token_id": tokenizer.pad_token_id,
            "bos_token_id": tokenizer.bos_token_id,
            "eos_token_id": tokenizer.eos_token_id,
        }
        args.update(generate_args)
        output_ids = model.generate(
            token_ids.to(model.device),
            **args
        )
    return tokenizer.decode(
        output_ids.tolist()[0][token_ids.size(1):]
        ).strip().rstrip("</s>")


def main(tokenizer_model: str, model: str, inputPath: str, inputLanguage: str, outputPath: str):
    with open(inputPath) as f:
        text = f.read()
    if model.endswith("-16k"):
        generate_args = {"max_new_tokens": 1024 * 16}
    else:
        generate_args = {}
    ret = run(tokenizer_model, model, text, generate_args=generate_args)
    with open(outputPath, mode="w") as f:
        f.write(str(ret))


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--tokenizer_model", type=str, default=None)
    parser.add_argument("--model", type=str, default="lmsys/vicuna-7b-v1.5")
    parser.add_argument("--inputPath", type=str, default="./sample/input.txt")
    parser.add_argument("--inputLanguage", type=str, default="ja")
    parser.add_argument("--outputPath", type=str, default="./sample/output.txt")
    main(**vars(parser.parse_args()))

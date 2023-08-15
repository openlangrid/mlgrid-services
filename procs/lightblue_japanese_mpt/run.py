
def run(tokenizer_model_name: str, model_name: str, text: str):
    from transformers import AutoTokenizer, AutoConfig, AutoModelForCausalLM, pipeline
    import torch

    if not tokenizer_model_name:
        tokenizer_model_name = model_name

    tokenizer = AutoTokenizer.from_pretrained(
        tokenizer_model_name,
        trust_remote_code=True)

    config = AutoConfig.from_pretrained(
        model_name,
        trust_remote_code=True)
#    config.attn_config['attn_impl'] = 'triton'
    config.init_device = 'cuda:0'

    model = AutoModelForCausalLM.from_pretrained(
        model_name,
        config=config,
        torch_dtype=torch.float16,
        trust_remote_code=True
    )

    inputs = tokenizer(text, return_tensors="pt").to(device=model.device)
    outputs = model.generate(
        **inputs,
        temperature=0.7,
        max_new_tokens=256)
    return tokenizer.batch_decode(outputs, skip_special_tokens=True)[0]

#    pipe = pipeline("text-generation", model=model, tokenizer=tokenizer)
#    output = pipe(text,
#        temperature=0.7,
#        do_sample=False,
 #       return_full_text=False,
 #       max_new_tokens=256)
 #   return output[0]["generated_text"]


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
    parser.add_argument("--model", type=str, default="lightblue/japanese-mpt-7b")
    parser.add_argument("--inputPath", type=str, default="./sample/input.txt")
    parser.add_argument("--inputLanguage", type=str, default="ja")
    parser.add_argument("--outputPath", type=str, default="./sample/output.txt")
    main(**vars(parser.parse_args()))

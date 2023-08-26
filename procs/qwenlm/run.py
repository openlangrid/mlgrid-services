def run(tokenizer_model_name: str, model_name: str, text: str):
    from transformers import AutoModelForCausalLM, AutoTokenizer
    from transformers.generation import GenerationConfig


    if not tokenizer_model_name:
        tokenizer_model_name = model_name

    tokenizer = AutoTokenizer.from_pretrained(
        tokenizer_model_name, trust_remote_code=True)
    model = AutoModelForCausalLM.from_pretrained(
        model_name, device_map="auto", trust_remote_code=True, fp16=True).eval()
    model.generation_config = GenerationConfig.from_pretrained(
        model_name, trust_remote_code=True)

    response, history = model.chat(tokenizer, text, history=None)
    return response


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
    parser.add_argument("--model", type=str, default="Qwen/Qwen-7B-Chat")
    parser.add_argument("--inputPath", type=str, default="./sample/input.txt")
    parser.add_argument("--inputLanguage", type=str, default="ja")
    parser.add_argument("--outputPath", type=str, default="./sample/output.txt")
    main(**vars(parser.parse_args()))

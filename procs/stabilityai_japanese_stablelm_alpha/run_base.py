
def main(tokenizer_model: str, model: str, inputPath: str, inputLanguage: str, outputPath: str):
    import torch
    import _run
    with open(inputPath) as f:
        text = f.read()
    ret = _run.run(tokenizer_model, model, text,
        model_args={"variant": "fp16", "torch_dtype": torch.float16})
    with open(outputPath, mode="w") as f:
        f.write(str(ret))


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--tokenizer_model", type=str, default="novelai/nerdstash-tokenizer-v1")
    parser.add_argument("--model", type=str, default="stabilityai/japanese-stablelm-base-alpha-7b")
    parser.add_argument("--inputPath", type=str, default="./sample/base_input.txt")
    parser.add_argument("--inputLanguage", type=str, default="ja")
    parser.add_argument("--outputPath", type=str, default="./sample/base_output.txt")
    main(**vars(parser.parse_args()))

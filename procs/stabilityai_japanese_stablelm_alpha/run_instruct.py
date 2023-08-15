
def main(tokenizer_model: str, model: str, inputPath: str, inputLanguage: str, outputPath: str):
    import _run
    with open(inputPath) as f:
        text = f.read()
    ret = _run.run(tokenizer_model, model, text)
    with open(outputPath, mode="w") as f:
        f.write(str(ret))


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--tokenizer_model", type=str, default="novelai/nerdstash-tokenizer-v1")
    parser.add_argument("--model", type=str, default="stabilityai/japanese-stablelm-instruct-alpha-7b")
    parser.add_argument("--inputPath", type=str, default="./sample/instruct_input.txt")
    parser.add_argument("--inputLanguage", type=str, default="ja")
    parser.add_argument("--outputPath", type=str, default="./sample/instruct_output.txt")
    main(**vars(parser.parse_args()))

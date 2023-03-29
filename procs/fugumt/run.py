import argparse, os

# models: 'staka/fugumt-ja-en', 'staka/fugumt-en-ja'

parser = argparse.ArgumentParser()
parser.add_argument("model", type=str, default="staka/fugumt-ja-en")
parser.add_argument("--inPath", type=str, default="./sample.txt")
parser.add_argument("--outPathPrefix", type=str, default="./temp/out")
args = parser.parse_args()

def main(model, inPath, outPathPrefix):
    from transformers import pipeline
    trans = pipeline('translation', model=model)
    with open(inPath) as f:
        text = f.read()
    result = trans(text)[0]["translation_text"]
    with open(f"{outPathPrefix}.result.txt", 'w', encoding='UTF-8') as f:
        f.write(result)
    print(result)

main(**vars(args))

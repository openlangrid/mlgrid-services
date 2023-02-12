import argparse, os

parser = argparse.ArgumentParser()
parser.add_argument("text", type=str, default="sunset over a lake in the mountains")
parser.add_argument("--textLang", type=str, default="en")
parser.add_argument("--targetLang", type=str, default="ja")
parser.add_argument("--outPathPrefix", type=str, default="temp/out")
args = parser.parse_args()

def main(text, textLang, targetLang, outPathPrefix):
    from transformers import pipeline
    if textLang == "ja" and targetLang == "en":
        model = 'staka/fugumt-ja-en'
    elif textLang == "en" and targetLang == "ja":
        model = 'staka/fugumt-en-ja'
    else:
        print(f"Error: no suitable model for {textLang} to {targetLang}")
        return
    trans = pipeline('translation', model=model)
    result = trans(text)[0]["translation_text"]
    with open(f"{outPathPrefix}.result.txt", 'w', encoding='UTF-8') as f:
        f.write(result)
    print(result)

main(**vars(args))

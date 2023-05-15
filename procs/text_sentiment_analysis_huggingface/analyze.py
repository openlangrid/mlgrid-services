
def run(model: str, tokenizer: str, input: str, inputLang):
    from transformers import pipeline, AutoModelForSequenceClassification, BertJapaneseTokenizer
    model = AutoModelForSequenceClassification.from_pretrained(model)
    tokenizer = BertJapaneseTokenizer.from_pretrained(tokenizer)
    nlp = pipeline("sentiment-analysis", model=model, tokenizer=tokenizer)
    return nlp(input)[0]


def main(model: str, tokenizer: str, inputPath: str, inputLanguage: str, outputPath: str):
    with open(inputPath) as f:
        text = f.read()
    ret = run(model, tokenizer, text, inputLanguage)
    with open(outputPath, mode="w") as f:
        import json
        json.dump(ret, f, ensure_ascii=False)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--model", type=str, default="koheiduck/bert-japanese-finetuned-sentiment")
    parser.add_argument("--tokenizer", type=str, default="cl-tohoku/bert-base-japanese-whole-word-masking")
    parser.add_argument("--inputPath", type=str, default="./sample/input.txt")
    parser.add_argument("--inputLanguage", type=str, default="en")
    parser.add_argument("--outputPath", type=str, default="./sample/output.txt")
    main(**vars(parser.parse_args()))

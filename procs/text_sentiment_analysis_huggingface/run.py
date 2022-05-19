from transformers import pipeline, AutoModelForSequenceClassification, BertJapaneseTokenizer
import sys, json

# パイプラインの準備
model = AutoModelForSequenceClassification.from_pretrained('daigo/bert-base-japanese-sentiment') 
tokenizer = BertJapaneseTokenizer.from_pretrained('cl-tohoku/bert-base-japanese-whole-word-masking')
nlp = pipeline("sentiment-analysis",model=model,tokenizer=tokenizer)


text = sys.argv[1] if len(sys.argv) > 1 else "今日はいい天気"

print(json.dumps(nlp(text), ensure_ascii=False))

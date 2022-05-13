from transformers import MarianMTModel
from transformers.models.marian.tokenization_marian import MarianTokenizer
import json
import sys

# Usage: runYoloV5.py model_name input_file

model_name = sys.argv[1] if len(sys.argv) > 1 else "opus-mt-en-jap"
input_file = sys.argv[2] if len(sys.argv) > 2 else "sample.txt"

with open(input_file) as f:
    src_text = [">>jap<< " + line.rstrip() for line in f if len(line.rstrip()) > 0]

tokenizer = MarianTokenizer.from_pretrained(f"Helsinki-NLP/{model_name}")
model = MarianMTModel.from_pretrained(f"Helsinki-NLP/{model_name}")
model.to("cuda:0")

for s in src_text:
    print(f"Src: {s}")
    data = tokenizer.prepare_seq2seq_batch([s], return_tensors="pt")
    data.to("cuda:0")
    result = tokenizer.decode(model.generate(**data)[0],
        skip_special_tokens=True, clean_up_tokenization_spaces=True)
    print(f"Result: {result}")
    del data

#data = tokenizer.prepare_seq2seq_batch(src_text, return_tensors="pt")
#data.to("cuda:0")
#translated = model.generate(**data)
#tgt_text = [tokenizer.decode(t, skip_special_tokens=True) for t in translated]
#print(json.dumps(tgt_text))

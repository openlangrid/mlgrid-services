import torch
from transformers import AutoTokenizer, AutoModelForCausalLM

tokenizer = AutoTokenizer.from_pretrained("matsuo-lab/weblab-10b-instruction-sft")
model = AutoModelForCausalLM.from_pretrained(
    "matsuo-lab/weblab-10b-instruction-sft",
    torch_dtype=torch.float16,
    device_map="auto")

#if torch.cuda.is_available():
#    model = model.to("cuda")

text = "大規模言語モデルについて説明してください。"
text = f'以下は、タスクを説明する指示です。要求を適切に満たす応答を書きなさい。\n\n### 指示:\n{text}\n\n### 応答:'
token_ids = tokenizer.encode(text, add_special_tokens=False, return_tensors="pt")
token_ids = token_ids.to(model.device)

with torch.no_grad():
    output_ids = model.generate(
        token_ids,
        max_new_tokens=512,
        do_sample=True,
        temperature=0.7,
        top_p=0.95
    )

output = tokenizer.decode(output_ids.tolist()[0][token_ids.size(1):])
print(output)

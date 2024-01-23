import torch
from transformers import AutoTokenizer, AutoModelForCausalLM

tokenizer = AutoTokenizer.from_pretrained("rinna/nekomata-14b-instruction", trust_remote_code=True)

# Use GPU with bf16
# model = AutoModelForCausalLM.from_pretrained("rinna/nekomata-14b-instruction", device_map="auto", trust_remote_code=True, bf16=True)

# Use GPU with fp16
model = AutoModelForCausalLM.from_pretrained("rinna/nekomata-14b-instruction", device_map="auto", trust_remote_code=True, fp16=True)

# Use CPU
# model = AutoModelForCausalLM.from_pretrained("rinna/nekomata-14b-instruction", device_map="cpu", trust_remote_code=True)

# Automatically select device and precision
model = AutoModelForCausalLM.from_pretrained("rinna/nekomata-14b-instruction", device_map="auto", trust_remote_code=True)

instruction = "次の日本語を英語に翻訳してください。"
input = "大規模言語モデル（だいきぼげんごモデル、英: large language model、LLM）は、多数のパラメータ（数千万から数十億）を持つ人工ニューラルネットワークで構成されるコンピュータ言語モデルで、膨大なラベルなしテキストを使用して自己教師あり学習または半教師あり学習によって訓練が行われる。"
prompt = f"""
以下は、タスクを説明する指示と、文脈のある入力の組み合わせです。要求を適切に満たす応答を書きなさい。

### 指示:
{instruction}

### 入力:
{input}

### 応答:
"""
token_ids = tokenizer.encode(prompt, add_special_tokens=False, return_tensors="pt")

with torch.no_grad():
    output_ids = model.generate(
        token_ids.to(model.device),
        max_new_tokens=200,
        do_sample=True,
        temperature=0.5,
        pad_token_id=tokenizer.pad_token_id,
        bos_token_id=tokenizer.bos_token_id,
        eos_token_id=tokenizer.eos_token_id
    )

output = tokenizer.decode(output_ids.tolist()[0])
print(output)

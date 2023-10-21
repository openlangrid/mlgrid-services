import torch
from peft import PeftModel, PeftConfig
from transformers import AutoTokenizer, AutoModelForCausalLM
peft_model_name = "llm-jp/llm-jp-13b-instruct-lora-jaster-dolly-oasst-v1.0"
tokenizer = AutoTokenizer.from_pretrained(peft_model_name)
config = PeftConfig.from_pretrained(peft_model_name)
model = AutoModelForCausalLM.from_pretrained(config.base_model_name_or_path, device_map="auto", torch_dtype=torch.float16)
model = PeftModel.from_pretrained(model, peft_model_name)
text = "自然言語処理とは何か"
text = text + "### 回答："
tokenized_input = tokenizer(text, add_special_tokens=False, return_tensors="pt").to(model.device)
with torch.no_grad():
    output = model.generate(
        **tokenized_input,
        max_new_tokens=100,
        do_sample=True,
        top_p=0.95,
        temperature=0.7,
    )[0]
#print(tokenizer.decode(output))
print(tokenizer.decode(output.tolist()[tokenized_input["input_ids"].size(1):]).rstrip("<EOD|LLM-jp>"))


def run(model_name: str, tokenizer_model_name: str):
    import torch
    from peft import PeftModel, PeftConfig
    from transformers import AutoTokenizer, AutoModelForCausalLM
    tokenizer = AutoTokenizer.from_pretrained(tokenizer_model_name)
    config = PeftConfig.from_pretrained(model_name)
    model = AutoModelForCausalLM.from_pretrained(config.base_model_name_or_path, device_map="auto", torch_dtype=torch.float16)
    model = PeftModel.from_pretrained(model, model_name)
    print("ready", flush=True)

    import json, sys
    for line in sys.stdin:
        input = json.loads(line)
        with open(input["textPath"]) as f:
            text = f.read()
        language = input["textLanguage"]
        outputPath = input["outputPath"]
#        text = "自然言語処理とは何か"
#        text = text + "### 回答："
        tokenized_input = tokenizer(
            text, add_special_tokens=False, return_tensors="pt").to(model.device)
        with torch.no_grad():
            output = model.generate(
                **tokenized_input,
                max_new_tokens=512,
                do_sample=True,
                top_p=0.95,
                temperature=0.7,
            )[0]
#                pad_token_id=tokenizer.pad_token_id,
#                eos_token_id=tokenizer.eos_token_id,
#            output = tokenizer.decode(
#                output_ids.tolist()[0][token_ids.size(1) :],
#                skip_special_tokens=True)
#        ret = ret.rstrip("<|endoftext|>")
        tokenized_input = tokenized_input["input_ids"]
        with open(outputPath, mode="w") as f:
            f.write(tokenizer.decode(output.tolist()[tokenized_input.size(1):]
                                     ).rstrip("<EOD|LLM-jp>"))
        print("ok", flush=True)


def main(model: str, tokenizerModel: str):
    run(model,
        tokenizerModel if tokenizerModel is not None else model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?", default="llm-jp/llm-jp-13b-instruct-lora-jaster-v1.0")
    parser.add_argument("--tokenizerModel", type=str, default=None)
    main(**vars(parser.parse_args()))

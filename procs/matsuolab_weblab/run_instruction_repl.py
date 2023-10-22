
def run(tokenizer_model_name: str, model_name: str):
    import torch
    from transformers import AutoTokenizer, AutoModelForCausalLM
    tokenizer = AutoTokenizer.from_pretrained(tokenizer_model_name)
    model = AutoModelForCausalLM.from_pretrained(
        model_name, torch_dtype=torch.float16, device_map="auto")
    print("ready", flush=True)

    import json, sys
    for line in sys.stdin:
        input = json.loads(line)
        systemPrompt = None
        if "systemPromptPath" in input:
            fname = input["systemPromptPath"]
            if len(fname) > 0:
                with open(fname) as f:
                    systemPrompt = f.read()
        if systemPrompt == None or len(systemPrompt) == 0:
            systemPrompt = "以下は、タスクを説明する指示です。要求を適切に満たす応答を書きなさい。"
        with open(input["userPromptPath"]) as f:
            userPrompt = f.read()
        promptLanguage = input["promptLanguage"]
        outputPath = input["outputPath"]
        prompt = f'{systemPrompt}\n\n### 指示:\n{userPrompt}\n\n### 応答:'
        with torch.no_grad():
            token_ids = tokenizer.encode(prompt, add_special_tokens=False, return_tensors="pt")
            token_ids = token_ids.to(model.device)
            output_ids = model.generate(
                token_ids,
                max_new_tokens=512,
                do_sample=True,
                temperature=0.7,
                top_p=0.95
#                pad_token_id=tokenizer.pad_token_id,
#                eos_token_id=tokenizer.eos_token_id,
            )
        output = tokenizer.decode(output_ids.tolist()[0][token_ids.size(1):])
        output = output.rstrip("<|endoftext|>")
        with open(outputPath, mode="w") as f:
            f.write(output)
        print("ok", flush=True)


def main(tokenizerModel: str, model: str):
    run(tokenizerModel if tokenizerModel is not None else model
        , model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?", default="elyza/ELYZA-japanese-Llama-2-7b-instruct")
    parser.add_argument("--tokenizerModel", type=str, default=None)
    main(**vars(parser.parse_args()))

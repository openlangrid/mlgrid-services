
def run(tokenizer_model_name: str, model_name: str):
    from transformers import AutoTokenizer, AutoModelForCausalLM
    tokenizer = AutoTokenizer.from_pretrained(tokenizer_model_name)
    model = AutoModelForCausalLM.from_pretrained(
        model_name,
        device_map="auto")
    model.eval()
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
        with open(input["userPromptPath"]) as f:
            userPrompt = f.read()
        promptLanguage = input["promptLanguage"]
        outputPath = input["outputPath"]
        if not systemPrompt or len(systemPrompt) == 0:
            systemPrompt = "以下は、タスクを説明する指示と、文脈のある入力の組み合わせです。要求を適切に満たす応答を書きなさい。"
        prompt = f"{systemPrompt}\n\n### 指示:\n{userPrompt}\n\n### 応答:\n"
        print(f"prompt: {prompt}", file=sys.stderr)
        input_ids = tokenizer.encode(
            prompt,
            add_special_tokens=False, 
            return_tensors="pt")
        tokens = model.generate(
            input_ids.to(device=model.device),
            max_new_tokens=1024,
            temperature=0.75,
            top_p=0.95,
            do_sample=True)
        out = tokenizer.decode(tokens[0][input_ids.shape[1]:], skip_special_tokens=True).strip()
        with open(outputPath, mode="w") as f:
            f.write(out)
        print("ok", flush=True)


def main(tokenizerModel: str, model: str):
    run(tokenizerModel if tokenizerModel is not None else model
        , model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?", default="mistralai/Mistral-7B-Instruct-v0.1")
    parser.add_argument("--tokenizerModel", type=str, default=None)
    main(**vars(parser.parse_args()))

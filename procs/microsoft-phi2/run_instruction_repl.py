
def run(tokenizer_model_name: str, model_name: str):
    import torch
    from transformers import AutoModelForCausalLM, AutoTokenizer
    torch.set_default_device("cuda")
    tokenizer = AutoTokenizer.from_pretrained(
        tokenizer_model_name, trust_remote_code=True)
    model = AutoModelForCausalLM.from_pretrained(
        model_name, torch_dtype="auto",
        device_map="auto", trust_remote_code=True)
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
        if systemPrompt == None:
            systemPrompt = ""
        with open(input["userPromptPath"]) as f:
            userPrompt = f.read()
        promptLanguage = input["promptLanguage"]
        outputPath = input["outputPath"]
        #systemPromptには対応していないので無視
        prompt = f"Instruct: {userPrompt}\nOutput: "
        inputs = tokenizer(prompt, return_tensors="pt", return_attention_mask=False)
        outputs = model.generate(**inputs, max_length=200)
        inputTokenLen = len(inputs["input_ids"][0])
        generated_text = tokenizer.decode(outputs[0][inputTokenLen:]).split("<|endoftext|>")[0]
        with open(outputPath, mode="w") as f:
            f.write(str(generated_text))
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

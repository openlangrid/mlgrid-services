
def run(tokenizer_model_name: str, model_name: str):
    import torch
    from transformers import AutoModelForCausalLM, AutoTokenizer
    tokenizer = AutoTokenizer.from_pretrained(tokenizer_model_name)
    model = AutoModelForCausalLM.from_pretrained(
        model_name, device_map="auto",
        torch_dtype=torch.float16)
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
        prompt = f"{systemPrompt}{userPrompt}### 回答："
        tokenized_input = tokenizer.encode(
            prompt, add_special_tokens=False, return_tensors="pt"
            ).to(model.device)
        with torch.no_grad():
            output_ids = model.generate(
                tokenized_input,
                max_new_tokens=512,
                do_sample=True,
                top_p=0.95,
                temperature=0.7,
            )[0]
        with open(outputPath, mode="w") as f:
            output_ids = output_ids.tolist()[tokenized_input.size(1):]
            f.write(tokenizer.decode(output_ids).rstrip("<EOD|LLM-jp>"))
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

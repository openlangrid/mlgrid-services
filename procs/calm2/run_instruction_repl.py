
def run(tokenizer_model_name: str, model_name: str):
    import torch
    from transformers import AutoModelForCausalLM, AutoTokenizer
    tokenizer = AutoTokenizer.from_pretrained(tokenizer_model_name)
    model = AutoModelForCausalLM.from_pretrained(
        model_name,
        device_map="auto",
        torch_dtype="auto")
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
            systemPrompt = ""
        with open(input["userPromptPath"]) as f:
            userPrompt = f.read()
        promptLanguage = input["promptLanguage"]
        outputPath = input["outputPath"]
        prompt = f"""{systemPrompt}USER: {userPrompt}
ASSISTANT: """
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
            generated_text = tokenizer.decode(
                output_ids,
                skip_special_tokens=True)
            f.write(generated_text)
        print("ok", flush=True)


def main(tokenizerModel: str, model: str):
    run(tokenizerModel if tokenizerModel is not None else model
        , model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?", default="cyberagent/calm2-7b-chat")
    parser.add_argument("--tokenizerModel", type=str, default=None)
    main(**vars(parser.parse_args()))

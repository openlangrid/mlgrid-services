
def run(tokenizer_model_name: str, model_name: str):
    from transformers import AutoTokenizer, AutoModelForCausalLM
    import torch

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

        messages = []
        if systemPrompt:
            messages.append({"role": "user", "content": systemPrompt})
        messages.append({"role": "user", "content": userPrompt})
        encodeds = tokenizer.apply_chat_template(messages, return_tensors="pt")
        model_inputs = encodeds.to(model.device)
        generated_ids = model.generate(
            model_inputs, max_new_tokens=1000, do_sample=True)
        generated_text = tokenizer.decode(generated_ids[0][model_inputs.shape[1]:])
        with open(outputPath, mode="w") as f:
            f.write(str(generated_text))

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

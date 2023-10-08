
def run(tokenizer_model_name: str, model_name: str):
    import torch
    from transformers import AutoModelForCausalLM, AutoTokenizer

    tokenizer = AutoTokenizer.from_pretrained(tokenizer_model_name)
    model = AutoModelForCausalLM.from_pretrained(model_name, torch_dtype="auto")
    if torch.cuda.is_available():
        model = model.to("cuda")

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
            systemPrompt = "あなたは誠実で優秀な日本人のアシスタントです。"
        with open(input["userPromptPath"]) as f:
            userPrompt = f.read()
        promptLanguage = input["promptLanguage"]
        outputPath = input["outputPath"]

        B_INST, E_INST = "[INST]", "[/INST]"
        B_SYS, E_SYS = "<<SYS>>\n", "\n<</SYS>>\n\n"
        prompt = f"{tokenizer.bos_token}{B_INST} {B_SYS}{systemPrompt}{E_SYS}{userPrompt} {E_INST}"

        with torch.no_grad():
            token_ids = tokenizer.encode(prompt, add_special_tokens=False, return_tensors="pt")

            output_ids = model.generate(
                token_ids.to(model.device),
                max_new_tokens=256,
                pad_token_id=tokenizer.pad_token_id,
                eos_token_id=tokenizer.eos_token_id,
            )
        output = tokenizer.decode(output_ids.tolist()[0][token_ids.size(1) :], skip_special_tokens=True)
#        ret = ret.rstrip("<|endoftext|>")
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

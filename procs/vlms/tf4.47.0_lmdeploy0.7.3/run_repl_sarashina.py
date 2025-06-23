
def run(tokenizer_model_name: str, model_name: str):
    from PIL import Image
    from transformers import AutoModelForCausalLM, AutoProcessor
    # Load model and processor
    processor = AutoProcessor.from_pretrained(tokenizer_model_name, trust_remote_code=True)
    model = AutoModelForCausalLM.from_pretrained(
        model_name,
        device_map="cuda",
        torch_dtype="auto",
        trust_remote_code=True,
    )
    print("ready", flush=True)


    from PIL import Image
    import json, sys
    for line in sys.stdin:
        input = json.loads(line)
        with open(input["systemPromptPath"]) as f:
            systemPrompt = f.read().strip()
        systemPromptLanguage = input["systemPromptLanguage"]
        with open(input["userPromptPath"]) as f:
            userPrompt = f.read().strip()
        userPromptlanguage = input["userPromptLanguage"]
        image = Image.open(input["imagePath"]).convert("RGB")
        outputPath = input["outputPath"]

        message = []
        if len(systemPrompt) > 0:
            message.append({"role": "system", "content": systemPrompt})
        message.append({"role": "user", "content": userPrompt})
        text_prompt = processor.apply_chat_template(message, add_generation_prompt=True)

        inputs = processor(
            text=[text_prompt],
            images=[image],
            padding=True,
            return_tensors="pt",
        )
        inputs = inputs.to("cuda")
        stopping_criteria = processor.get_stopping_criteria(["\n###"])

        # Inference: Generation of the output
        output_ids = model.generate(
            **inputs,
            max_new_tokens=128,
            temperature=0.0,
            do_sample=False,
            stopping_criteria=stopping_criteria,
        )
        generated_ids = [
            output_ids[len(input_ids) :] for input_ids, output_ids in zip(inputs.input_ids, output_ids)
        ]
        output_text = processor.batch_decode(
            generated_ids, skip_special_tokens=True, clean_up_tokenization_spaces=True
        )
        with open(outputPath, mode="w") as f:
            ret = output_text[0]
            f.write(ret)

        print("ok", flush=True)


def main(tokenizerModel: str, model: str):
    run(tokenizerModel if tokenizerModel is not None else model, model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?", default="sbintuitions/sarashina2-vision-8b")
    parser.add_argument("--tokenizerModel", type=str, default="sbintuitions/sarashina2-vision-8b")
    main(**vars(parser.parse_args()))

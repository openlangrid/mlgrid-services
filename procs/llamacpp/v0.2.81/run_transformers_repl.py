
def run(modelId: str):
    from torch.cuda import OutOfMemoryError
    from transformers import AutoTokenizer, AutoModelForCausalLM
    import torch
    dtype = torch.bfloat16

    try:
        tokenizer = AutoTokenizer.from_pretrained(modelId)
        model = AutoModelForCausalLM.from_pretrained(
            modelId,
            device_map="cuda",
            torch_dtype=dtype,)
        print("ready", flush=True)

        import json, sys
        for line in sys.stdin:
            input = json.loads(line)
            serviceType = input["serviceType"]
            methodName = input["methodName"]
            outputPath = input["outputPath"]
            if serviceType == "TextGenerationService" and methodName == "generate":
                with open(input["textPath"]) as f:
                    text = f.read()
                language = input["textLanguage"]
                sourceText = text

                input_ids = tokenizer(sourceText, return_tensors="pt").to("cuda")
                outputs = model.generate(
                    **input_ids,
                    max_new_tokens=1024)
                output = tokenizer.decode(outputs[0][len(input_ids[0]):])
            else:
                messages = []
                if serviceType == "TextInstructionService" and methodName == "generate":
                    if "systemPromptPath" in input:
                        fname = input["systemPromptPath"]
                        if len(fname) > 0:
                            with open(fname) as f:
                                messages.append({"role": "system", "content": f.read()})
                    with open(input["userPromptPath"]) as f:
                        messages.append({"role": "user", "content": f.read()})
                    promptLanguage = input["promptLanguage"]
                elif serviceType == "ContextualQuestionAnsweringService" and methodName == "ask":
                    question = input["question"]
                    context = input["context"]
                    language = input["language"]
                    messages.append({"role": "system", "content": "参考情報を元に、質問にできるだけ正確に答えてください。"})
                    messages.append({"role": "user", "content": f"{context}\n\n質問は次のとおりです。{question}"})
                elif serviceType == "ChatService" and methodName == "generate":
                    with open(input["messagesPath"]) as f:
                        messages = json.load(f)
                prompt = tokenizer.apply_chat_template(messages, tokenize=False, add_generation_prompt=True)
                inputs = tokenizer.encode(prompt, add_special_tokens=False, return_tensors="pt")
                outputs = model.generate(input_ids=inputs.to(model.device), max_new_tokens=1024)
                output = tokenizer.decode(outputs[0][len(inputs[0]):]).removesuffix(tokenizer.eos_token).rstrip()
                for t in tokenizer.additional_special_tokens:
                    output = output.removesuffix(t).rstrip()
            with open(outputPath, mode="w") as f:
                f.write(output)
            from gpuinfo import get_gpu_properties
            props = get_gpu_properties()
            print(f"ok {json.dumps(props)}", flush=True)
    except OutOfMemoryError:
        print("ng torch.cuda.OutOfMemoryError", flush=True)
    except RuntimeError:  # RuntimeError: "addmm_impl_cpu_" not implemented for 'Half'
        print("ng RuntimeError", flush=True)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("modelId", type=str, nargs="?", default="google/gemma-2-9b-it")
    run(**vars(parser.parse_args()))

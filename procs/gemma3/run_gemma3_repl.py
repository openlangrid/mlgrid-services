
def run(modelId: str):
    from torch.cuda import OutOfMemoryError
    from transformers import AutoProcessor, Gemma3ForConditionalGeneration
    import torch
    dtype = torch.bfloat16

    try:
        processor = AutoProcessor.from_pretrained(modelId)
        model = Gemma3ForConditionalGeneration.from_pretrained(
            modelId, device_map="auto"
        ).eval()
        print("ready", flush=True)

        import json, sys
        for line in sys.stdin:
            input = json.loads(line)
            serviceType = input["serviceType"]
            methodName = input["methodName"]
            outputPath = input["outputPath"]
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
            messages = [
                {"role": m["role"], "content": [{"type": "text", "text": m["content"]}]}
                    for m in messages]
            inputs = processor.apply_chat_template(
                messages, add_generation_prompt=True, tokenize=True,
                return_dict=True, return_tensors="pt"
                ).to(model.device, dtype=torch.bfloat16)
            input_len = inputs["input_ids"].shape[-1]
            with torch.inference_mode():
                generation = model.generate(**inputs, max_new_tokens=4096, do_sample=False)
                generation = generation[0][input_len:]
            output = processor.decode(generation, skip_special_tokens=True)
#                output = tokenizer.decode(outputs[0][len(inputs[0]):]).removesuffix(tokenizer.eos_token).rstrip()
#                for t in tokenizer.additional_special_tokens:
#                    output = output.removesuffix(t).rstrip()
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

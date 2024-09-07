
def run(modelId: str, gpuMemRatio: float):
    from torch.cuda import OutOfMemoryError
    from vllm import LLM, SamplingParams

    try:
        model = LLM(
            model=modelId, dtype="auto",
            trust_remote_code=True, #tensor_parallel_size=1,
            max_model_len=4096, #quantization="awq",
            gpu_memory_utilization=gpuMemRatio
        )
        tokenizer = model.get_tokenizer()
        generation_params = SamplingParams(
            temperature=0.8,
            top_p=0.95, top_k=40,
            max_tokens=4096,
            repetition_penalty=1.1)
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
                input_ids = tokenizer.encode(
                    sourceText,
                    add_special_tokens=True,)
                outputs = model.generate(
                    sampling_params=generation_params,
                    prompt_token_ids=[input_ids])
                output = outputs[0].outputs[0].text
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
                #####
                prompt = tokenizer.apply_chat_template(
                    conversation=messages,
                    add_generation_prompt=True,
                    tokenize=False)
                input_ids = tokenizer.encode(
                    prompt,
                    add_special_tokens=True,)
                # 推論
                outputs = model.generate(
                    sampling_params=generation_params,
                    prompt_token_ids=[input_ids])
                # Print the outputs.
                output = outputs[0].outputs[0].text
                #####
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
    parser.add_argument("gpuMemRatio", type=float, nargs="?", default="0.95")
    run(**vars(parser.parse_args()))

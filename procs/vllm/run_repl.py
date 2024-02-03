

def run(model_name: str):
    from torch.cuda import OutOfMemoryError
    from vllm import LLM, SamplingParams

    try:
        utils = {"14b": 0.9, "13b": 0.9, "7b": 0.9}
        u = 0.9
        for k in utils:
            if k in model_name:
                u = utils[k]
        llm = LLM(model=model_name, gpu_memory_utilization=u)
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
            elif serviceType == "TextInstructionService" and methodName == "generate":
                systemPrompt = None
                if "systemPromptPath" in input:
                    fname = input["systemPromptPath"]
                    if len(fname) > 0:
                        with open(fname) as f:
                            systemPrompt = f.read()
                if systemPrompt == None or len(systemPrompt) == 0:
                    systemPrompt = "あなたは誠実で優秀な日本人のアシスタントです。"
                with open(input["userPromptPath"]) as f:
                    userPrompt = f.read()
                promptLanguage = input["promptLanguage"]
                sourceText = f"<s>[INST] <<SYS>>\n{systemPrompt}\n<</SYS>>{userPrompt} [/INST]"
            elif serviceType == "ContextualQuestionAnsweringService" and methodName == "ask":
                question = input["question"]
                context = input["context"]
                language = input["language"]
                systemPrompt = "<<SYS>>\n参考情報を元に、質問にできるだけ正確に答えてください。\n<</SYS>>\n\n"
                contextAndQuestion = f"{context}\n質問は次のとおりです。{question}"
                sourceText = f"<s>[INST] {systemPrompt}{contextAndQuestion} [/INST] "
            outputs = llm.generate(
                [sourceText],
                sampling_params = SamplingParams(
                    temperature=0.7,
                    top_p=0.95,
                    max_tokens=512,
                )
            )
            with open(outputPath, mode="w") as f:
                f.write(outputs[0].outputs[0].text)
            from gpuinfo import get_gpu_properties
            props = get_gpu_properties()
            print(f"ok {json.dumps(props)}", flush=True)
    except OutOfMemoryError:
        print("ng torch.cuda.OutOfMemoryError", flush=True)
    except RuntimeError:  # RuntimeError: "addmm_impl_cpu_" not implemented for 'Half'
        print("ng RuntimeError", flush=True)


def main(model: str):
    run(model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?", default="elyza/ELYZA-japanese-Llama-2-7b-instruct")
    main(**vars(parser.parse_args()))

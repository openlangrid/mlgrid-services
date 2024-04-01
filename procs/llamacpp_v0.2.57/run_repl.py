
def run(orgModelId: str, ggufModelId: str, ggufFilePattern: str):
    from torch.cuda import OutOfMemoryError
    from llama_cpp import Llama, llama_chat_format

    try:
        chat_handler = llama_chat_format.hf_autotokenizer_to_chat_completion_handler(orgModelId)
        llm = Llama.from_pretrained(
            ggufModelId, filename=ggufFilePattern, n_gpu_layers=-1, n_ctx=1024,
            chat_handler=chat_handler)
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
                output = llm(sourceText,
                    temperature=0.7,
                    max_tokens=1024)
                output = output["choices"][0]["text"]
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
                output = llm.create_chat_completion(
                    messages=messages, max_tokens=1024)
                output = output["choices"][0]["message"]["content"]

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
    parser.add_argument("orgModelId", type=str, nargs="?", default="SakanaAI/EvoLLM-JP-A-v1-7B")
    parser.add_argument("ggufModelId", type=str, nargs="?", default="mmnga/SakanaAI-EvoLLM-JP-A-v1-7B-gguf")
    parser.add_argument("ggufFilePattern", type=str, nargs="?", default="*q5_K_M.gguf")
    run(**vars(parser.parse_args()))

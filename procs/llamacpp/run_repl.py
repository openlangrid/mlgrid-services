

def run(model_name: str):
    from torch.cuda import OutOfMemoryError
    from llama_cpp import Llama

    try:
        llm = Llama(
            model_path=model_name,
            n_gpu_layers=-1)
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
                if not systemPrompt == None and len(systemPrompt) > 0:
                    systemPrompt = f" <<SYS>>\n{systemPrompt}\n<</SYS>>"
                with open(input["userPromptPath"]) as f:
                    userPrompt = f.read()
                promptLanguage = input["promptLanguage"]
                sourceText = f"<s>[INST] {systemPrompt}{userPrompt} [ATTR] helpfulness: 4 correctness: 4 coherence: 4 complexity: 4 verbosity: 4 quality: 4 toxicity: 0 humor: 0 creativity: 0 [/ATTR] [/INST]"
            elif serviceType == "ContextualQuestionAnsweringService" and methodName == "ask":
                question = input["question"]
                context = input["context"]
                language = input["language"]
                systemPrompt = "<<SYS>>\n参考情報を元に、質問にできるだけ正確に答えてください。\n<</SYS>>\n\n"
                contextAndQuestion = f"{context}\n質問は次のとおりです。{question}"
                sourceText = f"<s>[INST] {systemPrompt}{contextAndQuestion} [/INST] "
            output = llm(sourceText,
                temperature=0.7,
                max_tokens=1024)
            output = output["choices"][0]["text"]
            with open(outputPath, mode="w") as f:
                f.write(output)
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
    parser.add_argument("model", type=str, nargs="?", default="./models/karakuri-lm-70b-chat-v0.1-q5_K_S.gguf")
    main(**vars(parser.parse_args()))

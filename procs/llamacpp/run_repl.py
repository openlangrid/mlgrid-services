def instructionFormatter(modelName: str):
    import sys
    f = "<s>[INST] <<SYS>>\\n{systemPrompt or 'あなたは誠実で優秀な日本人のアシスタントです。'}\\n<</SYS>>\\n\\n{userPrompt} [/INST] "
    if "ELYZA" in modelName:
        pass
    elif "calm2" in modelName and "chat" in modelName:
        f = "USER: {userPrompt}\\nASSISTANT: "
    elif "karakuri" in modelName:
        sysp = "'You are a helpful, respectful and honest assistant. Always answer as helpfully as possible, while being safe. Your answers should not include any harmful, unethical, racist, sexist, toxic, dangerous, or illegal content. Please ensure that your responses are socially unbiased and positive in nature.' + chr(92) + chr(92) + 'n' + chr(92) + chr(92) + 'nIf a question does not make any sense, or is not factually coherent, explain why instead of answering something not correct. If you do not know the answer to a question, please do not share false information.'"
        attr = "[ATTR] helpfulness: 4 correctness: 4 coherence: 4 complexity: 4 verbosity: 4 quality: 4 toxicity: 0 humor: 0 creativity: 0 [/ATTR]"
        f = "<s>[INST] <<SYS>>\\n{systemPrompt or " + sysp + "}\\n<</SYS>\\n\\n{userPrompt} " + attr + " [/INST] "
    elif "youri" in modelName and "chat" in modelName:
        f = "設定: {systemPrompt or 'あなたは誠実で優秀な日本人のアシスタントです。'}\\nユーザー: {userPrompt}\\nシステム: "
    elif "nekomata" in modelName or "youri" in modelName or "llm-jp" in modelName:
        f = "以下は、タスクを説明する指示です。要求を適切に満たす応答を書きなさい。\\n\\n### 指示:\\n{userPrompt}\\n\\n### 応答:\\n"
    print("using template: " + f, file=sys.stderr)
    return lambda systemPrompt, userPrompt, attr: eval(f'f"{f}"')


class LlamaChatMessageFormatter:
    def __init__(self, attr=""):
        self.attr = attr

    def format(self, messages):
        message = ""
        first = messages[0]
        sys = f"<<SYS>>\n{first['content']}\n<</SYS>>\n\n" if first["role"] == "system" else ""
        for m in messages:
            if m["role"] == "user":
                message += f"<s>[INST] {sys}{m['content']} {self.attr}[/INST] "
            elif m["role"] == "assistant":
                message += f"{m['content']} </s>"
        return message

class Clam2ChatMessageFormatter:
    def format(self, messages):
        message = ""
        for m in messages:
            if m["role"] == "user":
                message += f"USER: {m['content']}\n"
            elif m["role"] == "assistant":
                message += f"ASSISTANT: {m['content']}\n"
        message += "ASSISTANT: "
        return message

class YouriChatMessageFormatter:
    def format(self, messages):
        message = ""
        for m in messages:
            if m["role"] == "system":
                message += f"設定: {m['content']}\n"
            elif m["role"] == "user":
                message += f"ユーザー: {m['content']}\n"
            elif m["role"] == "assistant":
                message += f"システム: {m['content']}\n"
        message += "システム: "
        return message

class OpenAIChatMessageFormatter:
    def format(self, messages):
        message = ""
        for m in messages:
            if m["role"] == "system":
                message += f"<|im_start|>system\n{m['content']}<|im_end|>\n"
            elif m["role"] == "user":
                message += f"<|im_start|>user\n{m['content']}<|im_end|>\n"
            elif m["role"] == "assistant":
                message += f"<|im_start|>assistant\n{m['content']}<|im_end|>\n"
        message += "<|im_start|>assistant\n"
        return message

def chatFormatter(modelName: str):
    if "calm2" in modelName and "chat" in modelName:
        return Clam2ChatMessageFormatter()
    if "karakuri" in modelName:
        return LlamaChatMessageFormatter("[ATTR] helpfulness: 4 correctness: 4 coherence: 4 complexity: 4 verbosity: 4 quality: 4 toxicity: 0 humor: 0 creativity: 0 [/ATTR]")
    if "youri" in modelName and "chat" in modelName:
        return YouriChatMessageFormatter()
    if "qwen" in modelName and "chat" in modelName:
        return OpenAIChatMessageFormatter()
    return LlamaChatMessageFormatter()


def run(modelName: str):
    from torch.cuda import OutOfMemoryError
    from llama_cpp import Llama

    try:
        llm = Llama(
            model_path=modelName,
            n_gpu_layers=-1,
            n_ctx=1024)
        print("ready", flush=True)

        import json, sys
        for line in sys.stdin:
            input = json.loads(line)
            serviceType = input["serviceType"]
            methodName = input["methodName"]
            outputPath = input["outputPath"]
            attr = " [ATTR] helpfulness: 4 correctness: 4 coherence: 4 complexity: 4 verbosity: 4 quality: 4 toxicity: 0 humor: 0 creativity: 0 [/ATTR]" if "karakuri" in modelName else ""
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
                with open(input["userPromptPath"]) as f:
                    userPrompt = f.read()
                promptLanguage = input["promptLanguage"]
                sourceText = instructionFormatter(modelName)(systemPrompt, userPrompt, attr)
            elif serviceType == "ContextualQuestionAnsweringService" and methodName == "ask":
                question = input["question"]
                context = input["context"]
                language = input["language"]
                systemPrompt = "<<SYS>>\n参考情報を元に、質問にできるだけ正確に答えてください。\n<</SYS>>\n\n"
                contextAndQuestion = f"{context}\n質問は次のとおりです。{question}"
                sourceText = f"<s>[INST] {systemPrompt}{contextAndQuestion}{attr} [/INST] "
            elif serviceType == "ChatService" and methodName == "generate":
                with open(input["messagesPath"]) as f:
                    messages = json.load(f)
                sourceText = chatFormatter(modelName).format(messages)

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

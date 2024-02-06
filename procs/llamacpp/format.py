s = ""
print(s or "empty")
s = None
print(s or "s is None")

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
    elif "nekomata" in modelName or "youri" in modelName:
        f = "以下は、タスクを説明する指示です。要求を適切に満たす応答を書きなさい。\\n\\n### 指示:\\n{userPrompt}\\n\\n### 応答:\\n"
    print("using template: " + f, file=sys.stderr)
    return lambda systemPrompt, userPrompt, attr: eval(f'f"{f}"')

print(instructionFormatter("karakuri-100b")(
    "",
    "ユーザープロンプト",
    ""))
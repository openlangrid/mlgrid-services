B_INST, E_INST = "[INST]", "[/INST]"
B_SYS, E_SYS = "<<SYS>>\n", "\n<</SYS>>\n\n"
systemPrompt = "あなたは誠実で優秀な日本人のアシスタントです。"
userPrompt = "クマが海辺に行ってアザラシと友達になり、最終的には家に帰るというプロットの短編小説を書いてください。"
prompt = f"<s>{B_INST} {B_SYS}{systemPrompt}{E_SYS}{userPrompt} {E_INST}"
print(prompt)
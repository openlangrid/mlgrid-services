from vllm import LLM

# LLMの準備
llm = LLM(model="elyza/ELYZA-japanese-Llama-2-13b-instruct")


import string

# プロンプトテンプレートの準備
template = string.Template("""<s>[INST] <<SYS>>
あなたは誠実で優秀な日本人のアシスタントです。
<</SYS>>

${instruct} [/INST] """)


from vllm import SamplingParams

# プロンプトの準備
prompts = [
    "まどか☆マギカでは誰が一番かわいい?",
]

for i in range(len(prompts)):
    prompts[i] =  template.safe_substitute({"instruct": prompts[i]})

# 推論の実行
outputs = llm.generate(
    prompts,
    sampling_params = SamplingParams(
        temperature=0.5,
        max_tokens=256,
    )
)
for output in outputs:
    print("Prompt:", output.prompt, "\n")
    print("Response:", output.outputs[0].text, "\n----\n")

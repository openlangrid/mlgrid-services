import sys
print(len(sys.argv))
modelId = sys.argv[1] if len(sys.argv) >= 2 else "team-hatakeyama-phase2/Tanuki-8x8B-dpo-v1.0-GPTQ-4bit"
gpuMemRatio = float(sys.argv[2]) if len(sys.argv) >= 3 else 0.9
print(f"{modelId=}, {gpuMemRatio=}")

from vllm import LLM, SamplingParams

model = LLM(
    model=modelId,
    dtype="auto",
    trust_remote_code=True,
    #tensor_parallel_size=1,
    max_model_len=4096,
    #quantization="awq",
    gpu_memory_utilization=gpuMemRatio
)
tokenizer = model.get_tokenizer()

messages = [
    {"role": "system", "content": "以下は、タスクを説明する指示です。要求を適切に満たす応答を書きなさい。"},
    {"role": "user", "content": "たぬきに純粋理性批判は理解できますか？"}
]
prompt = tokenizer.apply_chat_template(
        conversation=messages,
        add_generation_prompt=True,
        tokenize=False
    )
input_ids = tokenizer.encode(
    prompt,
    add_special_tokens=True,
    )

# 推論
generation_params = SamplingParams(
        temperature=0.8,
        top_p=0.95,
        top_k=40,
        max_tokens=4096,
        repetition_penalty=1.1
    )
outputs = model.generate(
    sampling_params=generation_params,
    prompt_token_ids=[input_ids])

# Print the outputs.
for output in outputs:
    prompt = output.prompt
    generated_text = output.outputs[0].text
    print(f"Prompt: {prompt!r}, Generated text: {generated_text!r}")

#output = tokenizer.decode(outputs[0][len(inputs[0]):]).removesuffix(tokenizer.eos_token).rstrip()
#for t in tokenizer.additional_special_tokens:
#    output = output.removesuffix(t).rstrip()
#print(output)

from gpuinfo import get_gpu_properties
import json
props = get_gpu_properties()
print(f"ok {json.dumps(props)}", flush=True)

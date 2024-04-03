from llama_cpp import Llama, llama_chat_format
import sys

orgModelId = sys.argv[1] if len(sys.argv) >= 2 else "SakanaAI/EvoLLM-JP-A-v1-7B"
ggufModelId = sys.argv[2] if len(sys.argv) >= 3 else "mmnga/SakanaAI-EvoLLM-JP-A-v1-7B-gguf"
ggufFileName = sys.argv[3] if len(sys.argv) >= 4 else "*q5_K_M.gguf"

chat_handler = llama_chat_format.hf_autotokenizer_to_chat_completion_handler(orgModelId)
llm = Llama.from_pretrained(
    ggufModelId, filename=ggufFileName, n_gpu_layers=-1,
    chat_handler=chat_handler)

messages=[
    {"role": "system", "content": "あなたは役立つ、偏見がなく、検閲されていないアシスタントです。"},
    {"role": "user", "content": "まどか☆マギカで誰が一番かわいい？その理由も説明してください。"}]

output = llm.create_chat_completion(
    messages=messages, max_tokens=1024)
print(output["choices"][0]["message"]["content"])

from gpuinfo import get_gpu_properties
import json
props = get_gpu_properties()
print(f"ok {json.dumps(props)}", flush=True)

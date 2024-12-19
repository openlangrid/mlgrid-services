import sys
sys.path.append('/workspace/reasoning-model') 

model_name = sys.argv[1] if len(sys.argv) > 1 else "HachiML/QwQ-CoT-0.5B-JA"
gguf_file = sys.argv[2] if len(sys.argv) > 2 else None

# モジュールのインポート
from reasoning_model import ReasoningModelForCausalLM
from tree_utils import print_tree_with_best_path
from transformers import AutoTokenizer
import torch

# tokenizerとmodelの準備
tokenizer = AutoTokenizer.from_pretrained(model_name, gguf_file=gguf_file)
model = ReasoningModelForCausalLM.from_pretrained(
    model_name,
    gguf_file=gguf_file,
    torch_dtype=torch.bfloat16,
    device_map="cuda"
)

system_prompt = "You are a helpful and harmless assistant. You should think step-by-step."  # 固定を推奨
prompt = "次の式を計算してください。231 + 65*6 = ?"
#"messages=[
#    {"role": "system", "content": "あなたは役立つ、偏見がなく、検閲されていないアシスタントです。"},
#    {"role": "user", "content": "まどか☆マギカで誰が一番かわいい？その理由も説明してください。"}]
#"
messages = [
#    {"role": "system", "content": system_prompt},
    {"role": "user", "content": prompt}
]
def addSystemMessageIfAbsent(messages):
    if messages[0]["role"] != "system":
        return [
            {"role": "system", "content": "You are a helpful and harmless assistant. You should think step-by-step."},
            *messages 
            ]
    return messages
messages = addSystemMessageIfAbsent(messages)
print(messages)

# chat_templateとtokenize
text = tokenizer.apply_chat_template(
    messages,
    tokenize=False,
    add_generation_prompt=True
)
model_inputs = tokenizer([text], return_tensors="pt").to(model.device)

# MCTSを用いて生成（Google ColabのT4インスタンスで1分程度かかる）
final_tokens, final_node = model.generate(
    **model_inputs,
    iterations_per_step=5,      # 1推論ステップの探索に何回シミュレーションを行うか。長いほど精度が高まる可能性はあるが、推論時間が伸びる。
    max_iterations=15,          # 推論ステップの上限: 0.5Bモデルの場合、そこまで長いステップの推論はできないため10~15くらいが妥当。
    mini_step_size=32,          # mini-step: 32tokens。Step as Action戦略を採用する場合、ここを512など大きな数字にする。（実行時間が伸びるため非推奨）
    expand_threshold=0,         # ノードを拡張するために必要なそのノードの訪問回数の閾値。デフォルトの0で、拡張には1回以上の訪問が求められる。基本デフォルトで良い。
    step_separator_ids=None,    # Reasoning Action StrategyでStep as Actionを採用するときの区切りとなるトークンのIDリスト。NoneでモデルConfigの値を利用するため、変更は非推奨。Step as Action不採用時には[]を設定する。
)

# 結果をテキスト化
final_text = tokenizer.decode(final_tokens, skip_special_tokens=True)
print("=== 最終生成テキスト ===")
print(final_text)


from gpuinfo import get_gpu_properties
import json
props = get_gpu_properties()
print(f"ok {json.dumps(props)}", flush=True)

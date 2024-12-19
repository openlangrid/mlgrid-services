
def run(modelId: str, ggufFile: str):
    from torch.cuda import OutOfMemoryError
    from reasoning_model import ReasoningModelForCausalLM
    from tree_utils import print_tree_with_best_path
    from transformers import AutoTokenizer
    import torch
    dtype = torch.bfloat16

    try:
        tokenizer = AutoTokenizer.from_pretrained(modelId, gguf_file=ggufFile)
        model = ReasoningModelForCausalLM.from_pretrained(
            modelId,
            gguf_file=ggufFile,
            torch_dtype=dtype,
            device_map="cuda"
        )
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

                input_ids = tokenizer(sourceText, return_tensors="pt").to("cuda")
                outputs = model.generate(
                    **input_ids,
                    max_new_tokens=1024)
                output = tokenizer.decode(outputs[0][len(input_ids[0]):])
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
                    messages = addSystemMessageIfAbsent(messages)
                prompt = tokenizer.apply_chat_template(messages, tokenize=False, add_generation_prompt=True)
#                model_inputs = tokenizer([text], return_tensors="pt").to(model.device)
                inputs = tokenizer.encode(prompt, add_special_tokens=False, return_tensors="pt")
                #outputs = model.generate(input_ids=inputs.to(model.device), max_new_tokens=1024)
                final_tokens, final_node = model.generate(
                    **inputs,
                    iterations_per_step=5,      # 1推論ステップの探索に何回シミュレーションを行うか。長いほど精度が高まる可能性はあるが、推論時間が伸びる。
                    max_iterations=15,          # 推論ステップの上限: 0.5Bモデルの場合、そこまで長いステップの推論はできないため10~15くらいが妥当。
                    mini_step_size=32,          # mini-step: 32tokens。Step as Action戦略を採用する場合、ここを512など大きな数字にする。（実行時間が伸びるため非推奨）
                    expand_threshold=0,         # ノードを拡張するために必要なそのノードの訪問回数の閾値。デフォルトの0で、拡張には1回以上の訪問が求められる。基本デフォルトで良い。
                    step_separator_ids=None,    # Reasoning Action StrategyでStep as Actionを採用するときの区切りとなるトークンのIDリスト。NoneでモデルConfigの値を利用するため、変更は非推奨。Step as Action不採用時には[]を設定する。
                    max_new_tokens=1024
                )

                # = tokenizer.decode(outputs[0][len(inputs[0]):]).removesuffix(tokenizer.eos_token).rstrip()
                #for t in tokenizer.additional_special_tokens:
                #    output = output.removesuffix(t).rstrip()
                output = tokenizer.decode(final_tokens, skip_special_tokens=True)
            with open(outputPath, mode="w") as f:
                f.write(output)
            from gpuinfo import get_gpu_properties
            props = get_gpu_properties()
            print(f"ok {json.dumps(props)}", flush=True)
    except OutOfMemoryError:
        print("ng torch.cuda.OutOfMemoryError", flush=True)
    except RuntimeError:  # RuntimeError: "addmm_impl_cpu_" not implemented for 'Half'
        print("ng RuntimeError", flush=True)

def addSystemMessageIfAbsent(messages):
    if messages[0]["role"] != "system":
        return [
            {"role": "system", "content": "You are a helpful and harmless assistant. You should think step-by-step."},
            *messages 
            ]
    return messages


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("modelId", type=str, nargs="?", default="HachiML/QwQ-CoT-0.5B-JA")
    parser.add_argument("ggufFile", type=str, nargs="?", default=None)
    run(**vars(parser.parse_args()))

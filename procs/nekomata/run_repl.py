bfloat16Types = set(["elyza/ELYZA-japanese-Llama-2-13b-fast-instruct",
                    "elyza/ELYZA-japanese-Llama-2-13b-instruct"])

def buildInstructionPrompt(bosToken: str, systemPrompt: str, userPrompt: str, language: str):
    if systemPrompt == None or len(systemPrompt) == 0:
        systemPrompt = "以下は、タスクを説明する指示です。要求を適切に満たす応答を書きなさい。"
    return f"""
{systemPrompt}

### 指示:
{userPrompt}

### 応答:
"""

def buildQuestionAnsweringPrompt(bosToken: str, question: str, context: str, language: str):
    systemPrompt = "<<SYS>>\n参考情報を元に、質問にできるだけ正確に答えてください。\n<</SYS>>\n\n"
    contextAndQuestion = f"{context}\n質問は次のとおりです。{question}"
    return f"{bosToken}[INST] {systemPrompt}{contextAndQuestion} [/INST] "



def run(tokenizer_model_name: str, model_name: str):
    import torch
    from torch.cuda import OutOfMemoryError
    from transformers import AutoModelForCausalLM, AutoTokenizer
    try:
        tokenizer = AutoTokenizer.from_pretrained(
            tokenizer_model_name,
            trust_remote_code=True)
        model = AutoModelForCausalLM.from_pretrained(
            model_name,
            device_map="cuda", trust_remote_code=True, bf16=True,
            use_cache=True, low_cpu_mem_usage=True)
        model.eval()
        print("ready", flush=True)

        import json, sys
        for line in sys.stdin:
            try:
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
                    with open(input["userPromptPath"]) as f:
                        userPrompt = f.read()
                    promptLanguage = input["promptLanguage"]
                    sourceText = buildInstructionPrompt(tokenizer.bos_token, systemPrompt, userPrompt, promptLanguage)
                elif serviceType == "ContextualQuestionAnsweringService" and methodName == "ask":
                    question = input["question"]
                    context = input["context"]
                    language = input["language"]
                    sourceText = buildQuestionAnsweringPrompt(tokenizer.bos_token, question, context, language)
                token_ids = tokenizer.encode(
                    sourceText, add_special_tokens=False, return_tensors="pt"
                    ).to(model.device)
                with torch.no_grad():
                    output_ids = model.generate(
                        token_ids,
                        max_new_tokens=512, do_sample=True, top_p=0.95, temperature=0.7,
                        pad_token_id=tokenizer.pad_token_id,
                        bos_token_id=tokenizer.bos_token_id,
                        eos_token_id=tokenizer.eos_token_id
                    )
                output = tokenizer.decode(
                    output_ids.tolist()[0][token_ids.size(1) :],
                    skip_special_tokens=True)
                with open(outputPath, mode="w") as f:
                    f.write(output)
                from gpuinfo import get_gpu_properties
                props = get_gpu_properties()
                print(f"ok {json.dumps(props)}", flush=True)
            except OutOfMemoryError as e:
                raise e
            except Exception as e:
                print(e, file=sys.stderr)
                print("ng Exception", flush=True)
    except OutOfMemoryError:
        print("ng torch.cuda.OutOfMemoryError", flush=True)
    except RuntimeError:  # RuntimeError: "addmm_impl_cpu_" not implemented for 'Half'
        print("ng RuntimeError", flush=True)


def main(tokenizerModel: str, model: str):
    run(tokenizerModel if tokenizerModel is not None else model
        , model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?", default="rinna/nekomata-14b-instruction")
    parser.add_argument("--tokenizerModel", type=str, default=None)
    main(**vars(parser.parse_args()))

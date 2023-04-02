# 参考: https://github.com/kunishou/Japanese-Alpaca-LoRA


# parse args
import argparse
parser = argparse.ArgumentParser()
parser.add_argument("--utterancePath", nargs="?", type=str, default="input.txt")
parser.add_argument("--instructionPath", nargs="?", type=str, default="instruction.txt")
parser.add_argument("--outPathPrefix",type=str, default="input")
parser.add_argument("--model", nargs="?", type=str, default="decapoda-research/llama-7b-hf")
args = parser.parse_args()


def loadFile(path):
    import os
    if path and os.path.exists(path):
        with open(path) as f:
            return f.read()
    return None


# models:
# "decapoda-research/llama-7b-hf"
# "decapoda-research/llama-13b-hf"
# "decapoda-research/llama-30b-hf"
# "decapoda-research/llama-65b-hf"

def generate_prompt(instruction, utterance):
    if utterance and instruction:
        return f"""Below is an instruction that describes a task, paired with an input that provides further context. Write a response that appropriately completes the request.
### Instruction:
{instruction}
### Input:
{utterance}
### Response:"""
    elif utterance == None and instruction == None:
        return "Tell me about alpacas."
    else:
        return f"""Below is an instruction that describes a task. Write a response that appropriately completes the request.
### Instruction:
{instruction if instruction else utterance}
### Response:"""


def evaluate(
    model,
    device,
    tokenizer,
    instruction=None,
    input=None,
    temperature=0.1,
    top_p=0.75,
    top_k=40,
    num_beams=4,
    max_new_tokens=256,
    **kwargs,
):
    from transformers import GenerationConfig
    import torch
    prompt = generate_prompt(instruction, input)
    print(f"instruction: {instruction}")
    print(f"input: {input}")
    print(f"prompt: {prompt}")
    inputs = tokenizer(prompt, return_tensors="pt")
    input_ids = inputs["input_ids"].to(device)
    generation_config = GenerationConfig(
        temperature=temperature,
        top_p=top_p,
        top_k=top_k,
        num_beams=num_beams,
        no_repeat_ngram_size=3,
        **kwargs,
    )

    with torch.no_grad():
        generation_output = model.generate(
            input_ids=input_ids,
            generation_config=generation_config,
            return_dict_in_generate=True,
            output_scores=True,
            max_new_tokens=max_new_tokens,
        )
    s = generation_output.sequences[0]
    output = tokenizer.decode(s)
    return output.split("### Response:")[1].strip()


def main(instructionPath, utterancePath, outPathPrefix, model):
    import torch
    from peft import PeftModel
    import transformers

    assert (
        "LlamaTokenizer" in transformers._import_structure["models.llama"]
    ), "LLaMA is now in HuggingFace's main branch.\nPlease reinstall it: pip uninstall transformers && pip install git+https://github.com/huggingface/transformers.git"
    from transformers import LlamaTokenizer, LlamaForCausalLM

    tokenizer = LlamaTokenizer.from_pretrained(model, device_map={'': 0})

    loraWeights = {
        "decapoda-research/llama-7b-hf": "kunishou/Japanese-Alpaca-LoRA-7b-v0",
        "decapoda-research/llama-13b-hf": "kunishou/Japanese-Alpaca-LoRA-13b-v0",
        "decapoda-research/llama-30b-hf": "kunishou/Japanese-Alpaca-LoRA-30b-v0"}
    LORA_WEIGHTS = loraWeights.get(model, "kunishou/Japanese-Alpaca-LoRA-65b-v0")

    device = "cuda" if torch.cuda.is_available() else "cpu"
    try:
        if torch.backends.mps.is_available():
            device = "mps"
    except:
        pass

    if device == "cuda":
        model = LlamaForCausalLM.from_pretrained(
            model, torch_dtype=torch.float16, load_in_8bit=True,
            device_map={'': 0}, # device_map="auto",
        )
        model = PeftModel.from_pretrained(
            model, LORA_WEIGHTS, torch_dtype=torch.float16,
            device_map={'': 0},)
    elif device == "mps":
        model = LlamaForCausalLM.from_pretrained(
            model, torch_dtype=torch.float16,
            device_map={'': 0}, # device_map={"": device},
        )
        model = PeftModel.from_pretrained(
            model, LORA_WEIGHTS, torch_dtype=torch.float16,
            device_map={'': 0}, # device_map={"": device},
        )
    else:
        model = LlamaForCausalLM.from_pretrained(
            model, low_cpu_mem_usage=True,
            device_map={'': 0}, # device_map={"": device},
        )
        model = PeftModel.from_pretrained(
            model, LORA_WEIGHTS,
            device_map={'': 0}, # device_map={"": device},
        )

    model.eval()
    if torch.__version__ >= "2":
        model = torch.compile(model)

    instruction = loadFile(instructionPath)
    input = loadFile(utterancePath)
    result = evaluate(model, device, tokenizer, instruction, input)
    with open(f"{outPathPrefix}.result.txt", 'w', encoding='UTF-8') as f:
        f.write(result)
    print(result)


main(**vars(args))

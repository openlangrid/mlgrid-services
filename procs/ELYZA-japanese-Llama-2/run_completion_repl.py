bfloat16Types = set(["elyza/ELYZA-japanese-Llama-2-13b-fast-instruct",
                    "elyza/ELYZA-japanese-Llama-2-13b-instruct"])

def run(tokenizer_model_name: str, model_name: str):
    import torch
    from torch.cuda import OutOfMemoryError
    from transformers import AutoModelForCausalLM, AutoTokenizer
    try:
        tokenizer = AutoTokenizer.from_pretrained(tokenizer_model_name)
        model = AutoModelForCausalLM.from_pretrained(
            model_name,
            torch_dtype=torch.bfloat16 if model_name in bfloat16Types else torch.float16,
            device_map="cuda",
            use_cache=True,
            low_cpu_mem_usage=True,
            )
        model.eval()
        print("ready", flush=True)

        import json, sys
        for line in sys.stdin:
            input = json.loads(line)
            with open(input["textPath"]) as f:
                text = f.read()
            language = input["textLanguage"]
            outputPath = input["outputPath"]
            print(f"bos_token: {tokenizer.bos_token}", file=sys.stderr)
            token_ids = tokenizer.encode(
                text,
                add_special_tokens=False,
                return_tensors="pt").to(model.device)
            with torch.no_grad():
                output_ids = model.generate(
                    token_ids,
                    max_new_tokens=512,
                    do_sample=True,
                    top_p=0.95,
                    temperature=0.7,
                    pad_token_id=tokenizer.pad_token_id,
                    eos_token_id=tokenizer.eos_token_id,
                )
                output = tokenizer.decode(
                    output_ids.tolist()[0][token_ids.size(1) :],
                    skip_special_tokens=True)
    #        ret = ret.rstrip("<|endoftext|>")
            with open(outputPath, mode="w") as f:
                f.write(output)
            from gpuinfo import get_gpu_properties
            props = get_gpu_properties()
            print(f"ok {json.dumps(props)}", flush=True)
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
    parser.add_argument("model", type=str, nargs="?", default="elyza/ELYZA-japanese-Llama-2-7b-instruct")
    parser.add_argument("--tokenizerModel", type=str, default=None)
    main(**vars(parser.parse_args()))


def run(tokenizer_model_name: str, model_name: str, text: str, model_args: dict = {}):
    import torch
    from transformers import LlamaTokenizer, AutoModelForCausalLM

    print("load tokenizer")
    tokenizer = LlamaTokenizer.from_pretrained(
        tokenizer_model_name,
        additional_special_tokens=['▁▁'])
    print("load model")
    model = AutoModelForCausalLM.from_pretrained(
        model_name,
        trust_remote_code=True,
        **model_args
    )
    print("disable eval")
    model.eval()
    print("model to cuda")
    model = model.to("cuda")

    print("encode text")
    input_ids = tokenizer.encode(text, add_special_tokens=False, return_tensors="pt")
    print("generate text")
    tokens = model.generate(
        input_ids.to(device=model.device),
        max_new_tokens=256,
        temperature=1,
        top_p=0.95,
        do_sample=True,
    )
    return tokenizer.decode(
        tokens[0][input_ids.shape[1]:], skip_special_tokens=False
        ).strip().rstrip("<|endoftext|>")

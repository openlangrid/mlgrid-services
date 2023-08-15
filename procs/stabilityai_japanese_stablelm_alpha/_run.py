
def run(tokenizer_model_name: str, model_name: str, text: str):
    import torch
    from transformers import LlamaTokenizer, AutoModelForCausalLM

    tokenizer = LlamaTokenizer.from_pretrained(
        tokenizer_model_name,
        additional_special_tokens=['▁▁'])
    model = AutoModelForCausalLM.from_pretrained(
        model_name,
        trust_remote_code=True,
    )
    model.half()
    model.eval()
    model = model.to("cuda")

    input_ids = tokenizer.encode(text, add_special_tokens=False, return_tensors="pt")
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

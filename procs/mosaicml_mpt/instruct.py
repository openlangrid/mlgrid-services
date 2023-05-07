
def run(model: str, text: str) -> str:
    import torch
    from transformers import StoppingCriteria, StoppingCriteriaList
    from quick_pipeline import InstructionTextGenerationPipeline as pipeline

    generate = pipeline(
        model,
        torch_dtype=torch.bfloat16,
        trust_remote_code=True,
    )
    stop_token_ids = generate.tokenizer.convert_tokens_to_ids(["<|endoftext|>"])
    class StopOnTokens(StoppingCriteria):
        def __call__(self, input_ids: torch.LongTensor, scores: torch.FloatTensor, **kwargs) -> bool:
            for stop_id in stop_token_ids:
                if input_ids[0][-1] == stop_id:
                    return True
            return False
    stop = StopOnTokens()

    inputs = generate.tokenizer(
        generate.format_instruction(text), return_tensors="pt"
        ).to(generate.model.device)
    input_length = inputs.input_ids.shape[1]

    outputs = generate.model.generate(
        **inputs, 
        max_new_tokens=128, 
        do_sample=True, 
        temperature=0.7, 
        top_p=0.7, 
        top_k=50, 
        return_dict_in_generate=True,
        stopping_criteria=StoppingCriteriaList([stop]),
    )
    token = outputs.sequences[0, input_length:]
    return generate.tokenizer.decode(token)


def main(model: str, inputPath: str, inputLang: str, outputPath: str):
    with open(inputPath) as f:
        text = f.read()
    ret = run(model, text)
    with open(outputPath, mode="w") as f:
        f.write(str(ret))


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--model", type=str, default="mosaicml/mpt-7b-instruct")
    parser.add_argument("--inputPath", type=str, default="./sample/input.txt")
    parser.add_argument("--inputLang", type=str, default="en")
    parser.add_argument("--outputPath", type=str, default="./sample/output.txt")
    main(**vars(parser.parse_args()))

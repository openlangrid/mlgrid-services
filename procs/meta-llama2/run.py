def run(model_name: str, text: str):
    # get env var
    import os
    TOKEN = os.environ['HUGGINGFACE_TOKEN']

    from transformers import AutoTokenizer
    import transformers
    import torch

    tokenizer = AutoTokenizer.from_pretrained(
        model_name,
        use_auth_token=TOKEN)
    pipeline = transformers.pipeline(
        "text-generation",
        model=model_name,
        torch_dtype=torch.float16,
        device_map="auto",
    )

    sequences = pipeline(
        text,
        do_sample=True,
        top_k=10,
        num_return_sequences=1,
        eos_token_id=tokenizer.eos_token_id,
        max_length=200,
    )
    return sequences[0]['generated_text']


def main(model: str, inputPath: str, inputLanguage: str, outputPath: str):
    with open(inputPath) as f:
        text = f.read()
    ret = run(model, text)
    with open(outputPath, mode="w") as f:
        f.write(str(ret))


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--model", type=str, default="meta-llama/Llama-2-7b-chat-hf")
    parser.add_argument("--inputPath", type=str, default="./sample/input.txt")
    parser.add_argument("--inputLanguage", type=str, default="ja")
    parser.add_argument("--outputPath", type=str, default="./sample/output.txt")
    main(**vars(parser.parse_args()))

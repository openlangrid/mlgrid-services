
def run(tokenizer_model_name: str, model_name: str, video_blip_model_name: str):
    import torch
    from heron.models.video_blip import VideoBlipForConditionalGeneration, VideoBlipProcessor
    from transformers import LlamaTokenizer

    device = "cuda:0"

    max_length = 512        
    model = VideoBlipForConditionalGeneration.from_pretrained(
        model_name, torch_dtype=torch.float16, ignore_mismatched_sizes=True
    )

    model = model.half()
    model.eval()
    model.to(device)

    # prepare a processor
    processor = VideoBlipProcessor.from_pretrained(video_blip_model_name)
    tokenizer = LlamaTokenizer.from_pretrained(tokenizer_model_name, additional_special_tokens=['▁▁'])
    processor.tokenizer = tokenizer

    print("ready", flush=True)


    from PIL import Image
    import json, sys
    for line in sys.stdin:
        input = json.loads(line)
        with open(input["promptPath"]) as f:
            prompt = f.read()
        language = input["promptLanguage"]
        image = Image.open(input["imagePath"])
        outputPath = input["outputPath"]


        # do preprocessing
        prompt = f"##human: {prompt}\n##gpt: "
        inputs = processor(
            text=prompt,
            images=image,
            return_tensors="pt",
            truncation=True,
        )

        inputs = {k: v.to(device) for k, v in inputs.items()}
        inputs["pixel_values"] = inputs["pixel_values"].to(device, torch.float16)

        # set eos token
        eos_token_id_list = [
            processor.tokenizer.pad_token_id,
            processor.tokenizer.eos_token_id,
            int(tokenizer.convert_tokens_to_ids("##"))
        ]

        # do inference
        with torch.no_grad():
            out = model.generate(**inputs, max_length=256, do_sample=False, temperature=0., eos_token_id=eos_token_id_list, no_repeat_ngram_size=2)

        with open(outputPath, mode="w") as f:
            ret = processor.tokenizer.batch_decode(out)[0]
            print(f"process result:\n{out}", file=sys.stderr)
            ret = ret.lstrip("<|endoftext|>").rstrip("\n##")
            f.write(ret)

        print("ok", flush=True)


def main(tokenizerModel: str, model: str, videoBlipModel: str):
    run(tokenizerModel if tokenizerModel is not None else model
        , model, videoBlipModel)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?", default="turing-motors/heron-chat-blip-ja-stablelm-base-7b-v0")
    parser.add_argument("--tokenizerModel", type=str, default="novelai/nerdstash-tokenizer-v1")
    parser.add_argument("--videoBlipModel", type=str, default="Salesforce/blip2-opt-2.7b")
    main(**vars(parser.parse_args()))

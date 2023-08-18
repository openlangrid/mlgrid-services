from PIL import Image
import pillow_avif
def run(tokenizer_model_name: str, model_name: str, prompt: str, image: Image):
    import torch
    from transformers import LlamaTokenizer, AutoModelForVision2Seq, BlipImageProcessor

    # helper function to format input prompts
    def build_prompt(prompt="", sep="\n\n### "):
        sys_msg = "以下は、タスクを説明する指示と、文脈のある入力の組み合わせです。要求を適切に満たす応答を書きなさい。"
        p = sys_msg
        roles = ["指示", "応答"]
        user_query = "与えられた画像について、詳細に述べてください。"
        msgs = [": \n" + user_query, ": "]
        if prompt:
            roles.insert(1, "入力")
            msgs.insert(1, ": \n" + prompt)
        for role, msg in zip(roles, msgs):
            p += sep + role + msg
        return p

    # load model
    model = AutoModelForVision2Seq.from_pretrained(model_name, trust_remote_code=True)
    processor = BlipImageProcessor.from_pretrained(model_name)
    tokenizer = LlamaTokenizer.from_pretrained(tokenizer_model_name, additional_special_tokens=['▁▁'])
    device = "cuda" if torch.cuda.is_available() else "cpu"
    model.to(device)

    # prepare inputs
    image = image.convert("RGB")
    prompt = build_prompt(prompt)
    inputs = processor(images=image, return_tensors="pt")
    text_encoding = tokenizer(prompt, add_special_tokens=False, return_tensors="pt")
    text_encoding["qformer_input_ids"] = text_encoding["input_ids"].clone()
    text_encoding["qformer_attention_mask"] = text_encoding["attention_mask"].clone()
    inputs.update(text_encoding)

    # generate
    outputs = model.generate(
        **inputs.to(device, dtype=model.dtype),
        num_beams=5,
        max_new_tokens=32,
        min_length=1,
    )
    generated_text = tokenizer.batch_decode(outputs, skip_special_tokens=True)[0].strip()
    print(generated_text)
    # 桜と東京スカイツリー
    return generated_text


def main(tokenizerModel: str, model: str, inputPromptPath: str, inputPromptLanguage: str, inputImagePath: str, outputPath: str):
    with open(inputPromptPath) as f:
        prompt = f.read()
    image = Image.open(inputImagePath)
    ret = run(tokenizerModel, model, prompt, image)
    with open(outputPath, mode="w") as f:
        f.write(str(ret))


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--tokenizerModel", type=str, default="novelai/nerdstash-tokenizer-v1")
    parser.add_argument("--model", type=str, default="stabilityai/japanese-instructblip-alpha")
    parser.add_argument("--inputPromptPath", type=str, default="./sample/instruct_blip_input.txt")
    parser.add_argument("--inputPromptLanguage", type=str, default="ja")
    parser.add_argument("--inputImagePath", type=str, default="./sample/instruct_blip_input.avif")
    parser.add_argument("--outputPath", type=str, default="./sample/instruct_blip_output.txt")
    main(**vars(parser.parse_args()))

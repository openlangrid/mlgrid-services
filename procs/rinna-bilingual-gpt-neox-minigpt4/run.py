from PIL import Image
def run(model_name: str, prompt: str, image: Image):
    import torch
    from minigpt4.processors.blip_processors import Blip2ImageEvalProcessor
    from customized_mini_gpt4 import CustomizedMiniGPT4

    print(prompt)

    ckpt_path = "./checkpoint.pth"

    model = CustomizedMiniGPT4(gpt_neox_model=model_name)
    tokenizer = model.gpt_neox_tokenizer

    if torch.cuda.is_available():
        model = model.to("cuda")

    if ckpt_path is not None:
        print("Load BLIP2-LLM Checkpoint: {}".format(ckpt_path))
        ckpt = torch.load(ckpt_path, map_location="cpu")
        model.load_state_dict(ckpt['model'], strict=False)

    vis_processor = Blip2ImageEvalProcessor()

    raw_image = image.convert('RGB')
    image = vis_processor(raw_image).unsqueeze(0).to(model.device)
    image_emb = model.encode_img(image)

    embs = model.get_context_emb(prompt, [image_emb])

    output_ids = model.gpt_neox_model.generate(
        inputs_embeds=embs,
        max_new_tokens=512,
        do_sample=True,
        temperature=1.0,
        top_p=0.85,
        pad_token_id=tokenizer.pad_token_id,
        bos_token_id=tokenizer.bos_token_id,
        eos_token_id=tokenizer.eos_token_id
    )

    return tokenizer.decode(output_ids.tolist()[0], skip_special_tokens=True)


def main(model: str, inputPromptPath: str, inputImagePath: str, inputLanguage: str, outputPath: str):
    with open(inputPromptPath) as f:
        text = f.read()
    image = Image.open(inputImagePath)
    ret = run(model, text, image)
    with open(outputPath, mode="w") as f:
        f.write(str(ret))


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--model", type=str, default="rinna/bilingual-gpt-neox-4b")
    parser.add_argument("--inputPromptPath", type=str, default="./sample/input.txt")
    parser.add_argument("--inputImagePath", type=str, default="./sample/input.jpg")
    parser.add_argument("--inputLanguage", type=str, default="ja")
    parser.add_argument("--outputPath", type=str, default="./sample/output.txt")
    main(**vars(parser.parse_args()))

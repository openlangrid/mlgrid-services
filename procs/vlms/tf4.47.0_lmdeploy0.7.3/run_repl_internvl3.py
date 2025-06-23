
def run(model_name: str):
    from lmdeploy import pipeline, TurbomindEngineConfig, ChatTemplateConfig
    from lmdeploy.vl import load_image
    from lmdeploy.vl.utils import encode_image_base64
    pipe = pipeline(model_name,
                    backend_config=TurbomindEngineConfig(
                        session_len=16384, tp=1,
                        cache_max_entry_count=0.5),
                    chat_template_config=ChatTemplateConfig(model_name='internvl2_5'))
    print("ready", flush=True)

    from PIL import Image
    import json, sys
    for line in sys.stdin:
        input = json.loads(line)
        with open(input["systemPromptPath"]) as f:
            systemPrompt = f.read().strip()
        systemPromptLanguage = input["systemPromptLanguage"]
        with open(input["userPromptPath"]) as f:
            userPrompt = f.read().strip()
        userPromptlanguage = input["userPromptLanguage"]
        image = load_image(input["imagePath"])
        outputPath = input["outputPath"]

        message = []
        if len(systemPrompt) > 0:
            message.append({"role": "system", "content": systemPrompt})
        message.append({"role": "user", "content": [
            {"type": "text", "text": userPrompt},
            {"type": "image_url", "image_url": {"url": f"data:image/jpeg;base64,{encode_image_base64(image)}"}}]})
        #prompt = 'describe this image'
        #prompt = "明るく親しげな口調で、具体的に絵を褒めて、上達のアドバイスをしてください。"
        response = pipe(message)
        with open(outputPath, mode="w") as f:
            f.write(response.text)
        print("ok", flush=True)


def main(model: str):
    run(model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?", default="OpenGVLab/InternVL3-8B")
    main(**vars(parser.parse_args()))

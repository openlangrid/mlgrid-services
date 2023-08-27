
def run(model_name: str):
    from utils.generation import SAMPLE_RATE, generate_audio, preload_models
    from scipy.io.wavfile import write as write_wav

    # download and load all models
    preload_models()

    print("ready", flush=True)

    import json, sys
    for line in sys.stdin:
        input = json.loads(line)
        with open(input["textPath"]) as f:
            text = f.read()
        language = input["textLanguage"]
        outputPath = input["outputPath"]

        write_wav(outputPath, SAMPLE_RATE,
            generate_audio(text, prompt=model_name))

        print("ok", flush=True)


def main(model: str):
    run(model)


if __name__ == "__main__": 
    main
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?", default="dingzhen")
    main(**vars(parser.parse_args()))

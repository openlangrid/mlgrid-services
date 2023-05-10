
def run(t1: str, t2: str) -> float:
    import tensorflow_hub as hub
    import tensorflow as tf
    import tensorflow_text as text  # Needed for loading universal-sentence-encoder-cmlm/multilingual-preprocess
    import numpy as np

    preprocessor = hub.KerasLayer(
        "https://tfhub.dev/google/universal-sentence-encoder-cmlm/multilingual-preprocess/2")
    encoder = hub.KerasLayer("https://tfhub.dev/google/LaBSE/2")

    normalization = lambda embeds: embeds / np.linalg.norm(embeds, 2, axis=1, keepdims=True)
    embeds1 = normalization(encoder(preprocessor([t1]))["default"])
    embeds2 = normalization(encoder(preprocessor([t2]))["default"])
    return (np.matmul(embeds1, np.transpose(embeds2)))[0][0]


def main(model: str, input1Path: str, input1Lang: str,
        input2Path: str, input2Lang: str, outputPath: str):
    with open(input1Path) as f:
        t1 = f.read()
    with open(input2Path) as f:
        t2 = f.read()
    r = run(t1, t2)
    with open(outputPath, mode="w") as f:
        f.write(str(r))


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--model", type=str, default="default")
    parser.add_argument("--input1Path", type=str, default="./sample/input1.txt")
    parser.add_argument("--input1Lang", type=str, default="en")
    parser.add_argument("--input2Path", type=str, default="./sample/input2.txt")
    parser.add_argument("--input2Lang", type=str, default="en")
    parser.add_argument("--outputPath", type=str, default="./sample/output.txt")
    main(**(parser.parse_args().__dict__))

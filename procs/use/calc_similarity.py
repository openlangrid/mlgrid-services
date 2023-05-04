
def run(t1: str, t2: str) -> float:
    import tensorflow_hub as hub
    import numpy as np
    import tensorflow_text

    # for avoiding error
    import ssl
    ssl._create_default_https_context = ssl._create_unverified_context

    embed = hub.load("https://tfhub.dev/google/universal-sentence-encoder-multilingual/3")
    vecs = embed([t1, t2])
    cos_sim = lambda v1, v2 : np.dot(v1, v2) / (np.linalg.norm(v1) * np.linalg.norm(v2))
    return cos_sim(vecs[0], vecs[1])


def main(input1Path: str, input1Lang: str,
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
    parser.add_argument("--input1Path", type=str, required=False, default="./sample/input1.txt")
    parser.add_argument("--input1Lang", type=str, default="en")
    parser.add_argument("--input2Path", type=str, default="./sample/input2.txt")
    parser.add_argument("--input2Lang", type=str, default="en")
    parser.add_argument("--outputPath", type=str, default="./sample/output.txt")
    main(**(parser.parse_args().__dict__))

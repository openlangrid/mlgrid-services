
def run(model: str, t1: str, t2: str) -> float:
    import tensorflow_hub as hub
    import numpy as np
    import tensorflow_text

    # for avoiding error
    import ssl
    ssl._create_default_https_context = ssl._create_unverified_context

    prefix = "https://tfhub.dev/google/universal-sentence-encoder-multilingual"
    model_url = f"{prefix}{'-large' if model == 'large' else ''}/3"
    embed = hub.load(model_url)
#    vecs = embed([t1, t2])
#    cos_sim = lambda v1, v2 : np.dot(v1, v2) / (np.linalg.norm(v1) * np.linalg.norm(v2))
#    return cos_sim(vecs[0], vecs[1])
    return np.inner(embed(t1), embed(t2))[0][0]


def main(model: str, input1Path: str, input1Lang: str,
        input2Path: str, input2Lang: str, outputPath: str):
    with open(input1Path) as f:
        t1 = f.read()
    with open(input2Path) as f:
        t2 = f.read()
    r = run(model, t1, t2)
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

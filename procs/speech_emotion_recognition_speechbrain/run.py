import argparse, os

parser = argparse.ArgumentParser()
parser.add_argument("file", type=str, default="sunset over a lake in the mountains")
args = parser.parse_args()
file = args.file


from speechbrain.pretrained.interfaces import foreign_class
classifier = foreign_class(
    source="speechbrain/emotion-recognition-wav2vec2-IEMOCAP",
    pymodule_file="custom_interface.py", classname="CustomEncoderWav2vec2Classifier")

out_prob, score, index, text_lab = classifier.classify_file(file)

import json
print(json.dumps({"degree": score[0].item(), "label": text_lab[0]}))

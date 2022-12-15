import json
import pathlib
import sys
import torch

if len(sys.argv) == 1:
  print("Usage: runYoloV5.py model_name input_file\n", file=sys.stderr)
  sys.exit(1)

model_name = sys.argv[1]
input_file = sys.argv[2] if len(sys.argv) > 2 else "zidane.jpg"

model = torch.hub.load('ultralytics/yolov5', model_name)  # or yolov5n - yolov5x6, custom
file = pathlib.Path(input_file)

# Inference
results = model(file)

# Results
import cv2
h, w, _ = cv2.imread(file).shape
result = {"width": w, "height": h}
crops = []
for pred in results.pred:
  if pred.shape[0]:
    for *box, conf, cls in reversed(pred):  # xyxy, confidence, class
      label = f'{results.names[int(cls)]}'
      crops.append({
        'label': label,
        'conf': float(conf),
        'box': list(map(lambda t:float(t), box)),
        })
result["results"] = crops
print("Pred:", json.dumps(result))

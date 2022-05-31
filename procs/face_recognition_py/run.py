from lib2to3.pytree import _Results
import face_recognition
import sys
import json

file = sys.argv[1] if len(sys.argv) > 1 else "zidane.jpg"
model = sys.argv[2] if len(sys.argv) > 2 else "hog"  # or "cnn"

image = face_recognition.load_image_file(file)

result = face_recognition.face_locations(image, model=model)

with open(f"{file}.result.json", mode="w") as f:
  f.write(json.dumps(result))

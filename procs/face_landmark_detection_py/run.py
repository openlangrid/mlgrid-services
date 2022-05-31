import face_recognition
import sys
import json

file = sys.argv[1] if len(sys.argv) > 1 else "zidane.jpg"

image = face_recognition.load_image_file(file)

result = face_recognition.face_landmarks(image)

with open(f"{file}.result.json", mode="w") as f:
  f.write(json.dumps(result))

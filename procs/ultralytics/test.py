from collections import defaultdict

import cv2
from ultralytics import YOLO
from ultralytics.utils.plotting import Annotator, colors

# Dictionary to store tracking history with default empty lists
track_history = defaultdict(lambda: [])

# Load the YOLO model with segmentation capabilities
#model = YOLO("yolov8n-seg.pt")
#model = YOLO("yolov8x-seg.pt")
#model = YOLO("yolov8x-pose.pt")

import sys

in_file = sys.argv[1]
model_name = sys.argv[2] if len(sys.argv) > 2 else "yolov8x-seg.pt"


model = YOLO(model_name)
track_history = defaultdict(lambda: [])

person_ids = set()
labels = set()
cap = cv2.VideoCapture(in_file)
w, h, fps = (int(cap.get(x)) for x in (cv2.CAP_PROP_FRAME_WIDTH, cv2.CAP_PROP_FRAME_HEIGHT, cv2.CAP_PROP_FPS))
out = cv2.VideoWriter(f"{in_file}.{model_name}.out.mp4", cv2.VideoWriter_fourcc(*"mp4v"), fps, (w, h))
try:
    i = 0
    while True:
        # Read a frame from the video
        ret, im0 = cap.read()
        if not ret:
            print("Video frame is empty or video processing has been successfully completed.")
            break

        # Create an annotator object to draw on the frame
        annotator = Annotator(im0, line_width=2)

        # Perform object tracking on the current frame
#        results = model.track(im0, persist=True, tracker="bytetrack.yaml")
        results = model.track(im0, persist=True)

        # Check if tracking IDs and masks are present in the results
        if results[0].boxes.id is not None and results[0].masks is not None:
            # Extract masks and tracking IDs
            masks = results[0].masks.xy
            track_ids = results[0].boxes.id.int().cpu().tolist()
            cls_ids = results[0].boxes.cls.int().cpu().tolist()

            # Annotate each mask with its corresponding tracking ID and color
            print(f"m:{len(masks)}, t:{len(track_ids)}, c:{len(cls_ids)}")
            for mask, track_id, cls_id in zip(masks, track_ids, cls_ids):
                labels.add(results[0].names[cls_id])
                if results[0].names[cls_id] != 'person':
                    continue
                person_ids.add(track_id)
                annotator.seg_bbox(mask=mask, mask_color=colors(track_id, True), label=str(track_id))

        # Write the annotated frame to the output video
        out.write(im0)
finally:
    # Release the video writer and capture objects, and close all OpenCV windows
    out.release()
    cap.release()
    cv2.destroyAllWindows()

print(f"{len(person_ids)} persons.")
print(labels)
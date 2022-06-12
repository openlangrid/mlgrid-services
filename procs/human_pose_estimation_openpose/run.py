import cv2, json, sys
sys.path.insert(0, '/workspace/openpose/build/python');
from openpose import pyopenpose as op

infile = sys.argv[1] if len(sys.argv) > 1 else "/work/IP210215TAN000004000.jpeg"
print(infile)

# OpenPoseの利用準備
print("start")
params = dict()
params["model_folder"] = "/workspace/openpose/models/"
wrapper = op.WrapperPython()
wrapper.configure(params)
wrapper.start()

# 部位の名前定義
partNames = ["Nose", "Neck",
  "RShoulder", "RElbow", "RWrist",
  "LShoulder", "LElbow", "LWrist",
  "MHip",
  "RHip", "RKnee", "RAnkle",
  "LHip", "LKnee", "LAnkle",
  "REye", "LEye", "REar", "LEar",
  "LBigToe", "LSmallToe", "LHeel",
  "RBigToe", "RSmallToe", "RHeel",
  ]

# ポーズ推定
print("estimate")
datum = op.Datum()
imageToProcess = cv2.imread(infile)
datum.cvInputData = imageToProcess
wrapper.emplaceAndPop(op.VectorDatum([datum]))
result = list()
if len(datum.poseKeypoints) > 0:
  for i, kps in enumerate(datum.poseKeypoints):
    pose = dict()
    for j, kp in enumerate(kps):
      pose[partNames[j]] = kp.tolist()
    result.append(pose)

# 結果の書き出し
print("write result")
cv2.imwrite(infile + ".out.jpg", datum.cvOutputData)
with open(infile + ".out.json", 'w') as f:
  json.dump(result, f)

print("DONE")

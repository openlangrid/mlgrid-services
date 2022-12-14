# Some basic setup:
# Setup detectron2 logger
from detectron2.utils.logger import setup_logger
setup_logger()

def main(config, infile):
    # import some common libraries
    import numpy as np
    import os, json, cv2, random, torch
    # import some common detectron2 utilities
    from detectron2 import model_zoo
    from detectron2.engine import DefaultPredictor
    from detectron2.config import get_cfg
    from detectron2.utils.visualizer import Visualizer
    from detectron2.data import MetadataCatalog, DatasetCatalog

    im = cv2.imread(infile)

    cfg = get_cfg()
    # add project-specific config (e.g., TensorMask) here if you're not running a model in detectron2's core library
    cfg.merge_from_file(model_zoo.get_config_file(f"{config}"))
    cfg.MODEL.ROI_HEADS.SCORE_THRESH_TEST = 0.5  # set threshold for this model
    # Find a model from detectron2's model zoo. You can use the https://dl.fbaipublicfiles... url as well
    cfg.MODEL.WEIGHTS = model_zoo.get_checkpoint_url(f"{config}")
    predictor = DefaultPredictor(cfg)
    outputs = predictor(im)

    import os
    dir = f"{infile}_result"
    os.makedirs(dir, exist_ok=True)
    outtext = f"{dir}/result.json"
    outmaskprefix = f"{dir}/mask_"

    names = MetadataCatalog.get(cfg.DATASETS.TRAIN[0]).get("thing_classes", None)
    result = {"width": im.shape[1], "height": im.shape[0]}
    results = []
    from PIL import Image
    for i in range(len(outputs["instances"].pred_classes)):
        o = outputs["instances"]
        ret = {}
        box = list(map(lambda t:float(t), o.pred_boxes[i].tensor.cpu().numpy()[0]))
        ret["label"] = names[o.pred_classes[i]]
        ret["conf"] = o.scores[i].item()
        ret["box"] = box
        results.append(ret)

        bx, by, bx2, by2 = box
        bx = int(bx)
        by = int(by)
        bw = int(bx2) - bx
        bh = int(by2) - by
        mask = Image.new("L", (bw, bh))
        pixels = o.pred_masks[i].cpu().numpy()
        for y in range(by, by + bh):
            for x in range(bx, bx + bw):
                if pixels[y][x]:
                    mask.putpixel((x - bx, y - by), 255)
        mask.save(f"{outmaskprefix}{i:#02}.png")

    result["results"] = results
    with open(outtext, "w") as f:
        json.dump(result, f)

import argparse
p = argparse.ArgumentParser()
p.add_argument('--infile', type=str, default="./sample/input.jpg", help='source image file')
p.add_argument('--config', type=str, default='COCO-InstanceSegmentation/mask_rcnn_R_50_FPN_3x.yaml', help='config file name under COCO-InstanceSegmentation/')
opt = p.parse_args()
main(opt.config, opt.infile)

import colossalai, os, sys, torch
import torch.distributed as dist
from mmengine.runner import set_random_seed
from opensora.datasets import save_sample
from opensora.registry import MODELS, SCHEDULERS, build_module
from opensora.utils.config_utils import parse_configs
from opensora.utils.misc import to_torch_dtype
from opensora.acceleration.parallel_states import set_sequence_parallel_group
from colossalai.cluster import DistCoordinator

cfg = None
dtype = None
device = None
latent_size = None
model = None
model_args = None
text_encoder = None
scheduler = None
vae = None

def setup():
    global cfg, dtype, device, latent_size, model, model_args, text_encoder, scheduler, vae
    # ======================================================
    # 1. cfg and init distributed env
    # ======================================================
    cfg = parse_configs(training=False)
    print(cfg, file=sys.stderr)

    # init distributed
    colossalai.launch_from_torch({})
    coordinator = DistCoordinator()

    if coordinator.world_size > 1:
        set_sequence_parallel_group(dist.group.WORLD) 
        enable_sequence_parallelism = True
    else:
        enable_sequence_parallelism = False

    # ======================================================
    # 2. runtime variables
    # ======================================================
    torch.set_grad_enabled(False)
    torch.backends.cuda.matmul.allow_tf32 = True
    torch.backends.cudnn.allow_tf32 = True
    device = "cuda" if torch.cuda.is_available() else "cpu"
    dtype = to_torch_dtype(cfg.dtype)
    set_random_seed(seed=cfg.seed)

    # ======================================================
    # 3. build model & load weights
    # ======================================================
    # 3.1. build model
    input_size = (cfg.num_frames, *cfg.image_size)
    vae = build_module(cfg.vae, MODELS)
    latent_size = vae.get_latent_size(input_size)
    text_encoder = build_module(cfg.text_encoder, MODELS, device=device)  # T5 must be fp32
    model = build_module(
        cfg.model,
        MODELS,
        input_size=latent_size,
        in_channels=vae.out_channels,
        caption_channels=text_encoder.output_dim,
        model_max_length=text_encoder.model_max_length,
        dtype=dtype,
        enable_sequence_parallelism=enable_sequence_parallelism,
    )
    text_encoder.y_embedder = model.y_embedder  # hack for classifier-free guidance

    # 3.2. move to device & eval
    vae = vae.to(device, dtype).eval()
    model = model.to(device, dtype).eval()

    # 3.3. build scheduler
    scheduler = build_module(cfg.scheduler, SCHEDULERS)

    # 3.4. support for multi-resolution
    model_args = dict()
    if cfg.multi_resolution:
        image_size = cfg.image_size
        hw = torch.tensor([image_size], device=device, dtype=dtype).repeat(cfg.batch_size, 1)
        ar = torch.tensor([[image_size[0] / image_size[1]]], device=device, dtype=dtype).repeat(cfg.batch_size, 1)
        model_args["data_info"] = dict(ar=ar, hw=hw)

    # ======================================================
    # 4. inference
    # ======================================================
    save_dir = cfg.save_dir
    os.makedirs(save_dir, exist_ok=True)


def inference(prompt: str, save_path: str):
    global cfg, dtype, device, latent_size, model, model_args, text_encoder, scheduler, vae
    samples = scheduler.sample(
        model,
        text_encoder,
        z_size=(vae.out_channels, *latent_size),
        prompts=[prompt],
        device=device,
        additional_args=model_args,
    )
    samples = vae.decode(samples.to(dtype))

    if save_path.endswith(".mp4"):
        save_path = save_path[:-4]
    save_sample(samples[0], fps=cfg.fps, save_path=save_path)


def run(modelName: str):
    from torch.cuda import OutOfMemoryError
    try:
        setup()
        print("ready", flush=True)

        import json, sys
        for line in sys.stdin:
            input = json.loads(line)
            serviceType = input["serviceType"]
            methodName = input["methodName"]
            with open(input["promptPath"]) as f:
                text = f.read()
            language = input["promptLanguage"]
            outputPath = input["outputPath"]

            inference(text, outputPath)
            from gpuinfo import get_gpu_properties
            props = get_gpu_properties()
            print(f"ok {json.dumps(props)}", flush=True)
    except OutOfMemoryError:
        print("ng torch.cuda.OutOfMemoryError", flush=True)
    except RuntimeError:  # RuntimeError: "addmm_impl_cpu_" not implemented for 'Half'
        print("ng RuntimeError", flush=True)


def main(model: str):
    run(model)


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=str, nargs="?", default="OpenSora-v1-HQ-16x256x256.pth")
    main(**vars(parser.parse_args()))

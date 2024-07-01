from huggingface_hub import snapshot_download

dir="/workspace/MusePose/pretrained_weights"

snapshot_download(
    repo_id="TMElyralab/MusePose",
    revision="main",
    local_dir=f"{dir}",
    local_dir_use_symlinks=True,
    allow_patterns="*.pth"
)

snapshot_download(
    repo_id="stabilityai/sd-vae-ft-mse",
    revision="main",
    local_dir=f"{dir}/sd-vae-ft-mse",
    local_dir_use_symlinks=True,
    allow_patterns=["config.json", "diffusion_pytorch_model.bin"]
)

snapshot_download(
    repo_id="yzd-v/DWPose",
    revision="main",
    local_dir=f"{dir}/dwpose",
    local_dir_use_symlinks=True,
    allow_patterns=["dw-ll_ucoco_384.pth"]
)

snapshot_download(
    repo_id="lambdalabs/sd-image-variations-diffusers",
    revision="main",
    local_dir=f"{dir}/sd-image-variations-diffusers",
    local_dir_use_symlinks=True,
    allow_patterns=["unet/*"]
)

snapshot_download(
    repo_id="lambdalabs/sd-image-variations-diffusers",
    revision="main",
    local_dir=f"{dir}/",
    local_dir_use_symlinks=True,
    allow_patterns=["image_encoder/*"]
)

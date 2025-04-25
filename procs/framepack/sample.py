import os
#os.environ['HF_HOME'] = os.path.abspath(os.path.realpath(os.path.join(os.path.dirname(__file__), './hf_download')))

import torch
import traceback
import einops
import numpy as np
import argparse
import math
from PIL import Image

from diffusers import AutoencoderKLHunyuanVideo
from transformers import LlamaModel, CLIPTextModel, LlamaTokenizerFast, CLIPTokenizer
from diffusers_helper.hunyuan import encode_prompt_conds, vae_decode, vae_encode, vae_decode_fake
from diffusers_helper.utils import save_bcthw_as_mp4, crop_or_pad_yield_mask, soft_append_bcthw, resize_and_center_crop, generate_timestamp
from diffusers_helper.models.hunyuan_video_packed import HunyuanVideoTransformer3DModelPacked
from diffusers_helper.pipelines.k_diffusion_hunyuan import sample_hunyuan
from diffusers_helper.memory import cpu, gpu, get_cuda_free_memory_gb, move_model_to_device_with_memory_preservation, offload_model_from_device_for_memory_preservation, fake_diffusers_current_device, DynamicSwapInstaller, unload_complete_models, load_model_as_complete
from transformers import SiglipImageProcessor, SiglipVisionModel
from diffusers_helper.clip_vision import hf_clip_vision_encode
from diffusers_helper.bucket_tools import find_nearest_bucket


def parse_args():
    parser = argparse.ArgumentParser(description="Generate video from image and prompt using FramePack.")
    parser.add_argument('--image_path', type=str, required=True, default="enami1.jpg", help='Path to the input image.')
    parser.add_argument('--prompt', type=str, required=True, default="男が壁に聞き耳を立てている様子", help='Text prompt describing the desired video content.')
    parser.add_argument('--output_dir', type=str, default='./outputs/', help='Directory to save the output video and intermediate files.')
    parser.add_argument('--n_prompt', type=str, default="", help='Negative text prompt.')
    parser.add_argument('--seed', type=int, default=31337, help='Random seed for generation.')
    parser.add_argument('--total_second_length', type=float, default=5.0, help='Total length of the video in seconds.')
    parser.add_argument('--latent_window_size', type=int, default=9, help='Latent window size (should not change).')
    parser.add_argument('--steps', type=int, default=25, help='Number of sampling steps.')
    parser.add_argument('--cfg', type=float, default=1.0, help='CFG Scale (should not change).')
    parser.add_argument('--gs', type=float, default=10.0, help='Distilled CFG Scale.')
    parser.add_argument('--rs', type=float, default=0.0, help='CFG Re-Scale (should not change).')
    parser.add_argument('--gpu_memory_preservation', type=float, default=6.0, help='GPU memory preservation in GB.')
    parser.add_argument('--use_teacache', action='store_true', default=True, help='Use TeaCache for potentially faster generation.')
    parser.add_argument('--no_teacache', action='store_false', dest='use_teacache', help='Do not use TeaCache.')

    args = parser.parse_args()
    return args

@torch.no_grad()
def worker(input_image, prompt, n_prompt, seed, total_second_length, latent_window_size, steps, cfg, gs, rs, gpu_memory_preservation, use_teacache, outputs_folder, high_vram, text_encoder, text_encoder_2, tokenizer, tokenizer_2, vae, feature_extractor, image_encoder, transformer):
    total_latent_sections = (total_second_length * 30) / (latent_window_size * 4)
    total_latent_sections = int(max(round(total_latent_sections), 1))

    job_id = generate_timestamp()
    output_filename_final = None

    print("Starting video generation...")

    try:
        # Clean GPU
        if not high_vram:
            unload_complete_models(
                text_encoder, text_encoder_2, image_encoder, vae, transformer
            )

        # Text encoding
        print("Text encoding ...")
        if not high_vram:
            fake_diffusers_current_device(text_encoder, gpu)
            load_model_as_complete(text_encoder_2, target_device=gpu)

        llama_vec, clip_l_pooler = encode_prompt_conds(prompt, text_encoder, text_encoder_2, tokenizer, tokenizer_2)

        if cfg == 1:
            llama_vec_n, clip_l_pooler_n = torch.zeros_like(llama_vec), torch.zeros_like(clip_l_pooler)
        else:
            llama_vec_n, clip_l_pooler_n = encode_prompt_conds(n_prompt, text_encoder, text_encoder_2, tokenizer, tokenizer_2)

        llama_vec, llama_attention_mask = crop_or_pad_yield_mask(llama_vec, length=512)
        llama_vec_n, llama_attention_mask_n = crop_or_pad_yield_mask(llama_vec_n, length=512)

        # Processing input image
        print("Image processing ...")
        input_image_np = np.array(input_image)
        H, W, C = input_image_np.shape
        height, width = find_nearest_bucket(H, W, resolution=640)
        input_image_np = resize_and_center_crop(input_image_np, target_width=width, target_height=height)

        Image.fromarray(input_image_np).save(os.path.join(outputs_folder, f'{job_id}_input.png'))

        input_image_pt = torch.from_numpy(input_image_np).float() / 127.5 - 1
        input_image_pt = input_image_pt.permute(2, 0, 1)[None, :, None]

        # VAE encoding
        print("VAE encoding ...")
        if not high_vram:
            load_model_as_complete(vae, target_device=gpu)

        start_latent = vae_encode(input_image_pt, vae)

        # CLIP Vision
        print("CLIP Vision encoding ...")
        if not high_vram:
            load_model_as_complete(image_encoder, target_device=gpu)

        image_encoder_output = hf_clip_vision_encode(input_image_np, feature_extractor, image_encoder)
        image_encoder_last_hidden_state = image_encoder_output.last_hidden_state

        # Dtype
        llama_vec = llama_vec.to(transformer.dtype)
        llama_vec_n = llama_vec_n.to(transformer.dtype)
        clip_l_pooler = clip_l_pooler.to(transformer.dtype)
        clip_l_pooler_n = clip_l_pooler_n.to(transformer.dtype)
        image_encoder_last_hidden_state = image_encoder_last_hidden_state.to(transformer.dtype)

        # Sampling
        print("Start sampling ...")
        rnd = torch.Generator("cpu").manual_seed(seed)
        num_frames = latent_window_size * 4 - 3

        history_latents = torch.zeros(size=(1, 16, 1 + 2 + 16, height // 8, width // 8), dtype=torch.float32).cpu()
        history_pixels = None
        total_generated_latent_frames = 0

        latent_paddings = reversed(range(total_latent_sections))

        if total_latent_sections > 4:
            latent_paddings = [3] + [2] * (total_latent_sections - 3) + [1, 0]

        for i, latent_padding in enumerate(latent_paddings):
            is_last_section = latent_padding == 0
            latent_padding_size = latent_padding * latent_window_size

            print(f"Generating section {i+1}/{total_latent_sections} (padding={latent_padding}, last={is_last_section})...")
            # print(f'latent_padding_size = {latent_padding_size}, is_last_section = {is_last_section}')

            indices = torch.arange(0, sum([1, latent_padding_size, latent_window_size, 1, 2, 16])).unsqueeze(0)
            clean_latent_indices_pre, blank_indices, latent_indices, clean_latent_indices_post, clean_latent_2x_indices, clean_latent_4x_indices = indices.split([1, latent_padding_size, latent_window_size, 1, 2, 16], dim=1)
            clean_latent_indices = torch.cat([clean_latent_indices_pre, clean_latent_indices_post], dim=1)

            clean_latents_pre = start_latent.to(history_latents)
            clean_latents_post, clean_latents_2x, clean_latents_4x = history_latents[:, :, :1 + 2 + 16, :, :].split([1, 2, 16], dim=2)
            clean_latents = torch.cat([clean_latents_pre, clean_latents_post], dim=2)

            if not high_vram:
                unload_complete_models()
                move_model_to_device_with_memory_preservation(transformer, target_device=gpu, preserved_memory_gb=gpu_memory_preservation)

            if use_teacache:
                transformer.initialize_teacache(enable_teacache=True, num_steps=steps)
            else:
                transformer.initialize_teacache(enable_teacache=False)

            def callback(d):
                current_step = d['i'] + 1
                percentage = int(100.0 * current_step / steps)
                print(f"  Sampling Step: {current_step}/{steps} ({percentage}%)", end='\r')
                # No preview generation needed for CLI
                return

            generated_latents = sample_hunyuan(
                transformer=transformer,
                sampler='unipc',
                width=width,
                height=height,
                frames=num_frames,
                real_guidance_scale=cfg,
                distilled_guidance_scale=gs,
                guidance_rescale=rs,
                num_inference_steps=steps,
                generator=rnd,
                prompt_embeds=llama_vec,
                prompt_embeds_mask=llama_attention_mask,
                prompt_poolers=clip_l_pooler,
                negative_prompt_embeds=llama_vec_n,
                negative_prompt_embeds_mask=llama_attention_mask_n,
                negative_prompt_poolers=clip_l_pooler_n,
                device=gpu,
                dtype=torch.bfloat16,
                image_embeddings=image_encoder_last_hidden_state,
                latent_indices=latent_indices,
                clean_latents=clean_latents,
                clean_latent_indices=clean_latent_indices,
                clean_latents_2x=clean_latents_2x,
                clean_latent_2x_indices=clean_latent_2x_indices,
                clean_latents_4x=clean_latents_4x,
                clean_latent_4x_indices=clean_latent_4x_indices,
                callback=callback,
            )
            print() # Newline after step progress

            if is_last_section:
                generated_latents = torch.cat([start_latent.to(generated_latents), generated_latents], dim=2)

            total_generated_latent_frames += int(generated_latents.shape[2])
            history_latents = torch.cat([generated_latents.to(history_latents), history_latents], dim=2)

            if not high_vram:
                offload_model_from_device_for_memory_preservation(transformer, target_device=gpu, preserved_memory_gb=8)
                load_model_as_complete(vae, target_device=gpu)

            real_history_latents = history_latents[:, :, :total_generated_latent_frames, :, :]

            print("  Decoding latents...")
            if history_pixels is None:
                history_pixels = vae_decode(real_history_latents, vae).cpu()
            else:
                section_latent_frames = (latent_window_size * 2 + 1) if is_last_section else (latent_window_size * 2)
                overlapped_frames = latent_window_size * 4 - 3

                current_pixels = vae_decode(real_history_latents[:, :, :section_latent_frames], vae).cpu()
                history_pixels = soft_append_bcthw(current_pixels, history_pixels, overlapped_frames)

            if not high_vram:
                unload_complete_models()

            output_filename_final = os.path.join(outputs_folder, f'{job_id}.mp4') # Use a consistent final name
            print(f"  Saving video segment to {output_filename_final}...")
            save_bcthw_as_mp4(history_pixels, output_filename_final, fps=30)

            print(f"  Decoded. Current latent shape {real_history_latents.shape}; pixel shape {history_pixels.shape}")

            if is_last_section:
                break

    except Exception as e:
        traceback.print_exc()
        print(f"Error during generation: {e}")

    finally:
        if not high_vram:
            print("Unloading models...")
            unload_complete_models(
                text_encoder, text_encoder_2, image_encoder, vae, transformer
            )

    print("Video generation finished.")
    return output_filename_final

if __name__ == "__main__":
    args = parse_args()

    print("Arguments:")
    for k, v in vars(args).items():
        print(f"  {k}: {v}")

    print("\nInitializing...")

    free_mem_gb = get_cuda_free_memory_gb(gpu)
    high_vram = free_mem_gb > 60

    print(f'Free VRAM {free_mem_gb} GB')
    print(f'High-VRAM Mode: {high_vram}')

    # Load Models
    print("Loading models...")
    text_encoder = LlamaModel.from_pretrained("hunyuanvideo-community/HunyuanVideo", subfolder='text_encoder', torch_dtype=torch.float16).cpu()
    text_encoder_2 = CLIPTextModel.from_pretrained("hunyuanvideo-community/HunyuanVideo", subfolder='text_encoder_2', torch_dtype=torch.float16).cpu()
    tokenizer = LlamaTokenizerFast.from_pretrained("hunyuanvideo-community/HunyuanVideo", subfolder='tokenizer')
    tokenizer_2 = CLIPTokenizer.from_pretrained("hunyuanvideo-community/HunyuanVideo", subfolder='tokenizer_2')
    vae = AutoencoderKLHunyuanVideo.from_pretrained("hunyuanvideo-community/HunyuanVideo", subfolder='vae', torch_dtype=torch.float16).cpu()
    feature_extractor = SiglipImageProcessor.from_pretrained("lllyasviel/flux_redux_bfl", subfolder='feature_extractor')
    image_encoder = SiglipVisionModel.from_pretrained("lllyasviel/flux_redux_bfl", subfolder='image_encoder', torch_dtype=torch.float16).cpu()
    transformer = HunyuanVideoTransformer3DModelPacked.from_pretrained('lllyasviel/FramePackI2V_HY', torch_dtype=torch.bfloat16).cpu()
    print("Models loaded.")

    # Configure Models
    vae.eval()
    text_encoder.eval()
    text_encoder_2.eval()
    image_encoder.eval()
    transformer.eval()

    if not high_vram:
        vae.enable_slicing()
        vae.enable_tiling()

    transformer.high_quality_fp32_output_for_inference = True
    # print('transformer.high_quality_fp32_output_for_inference = True') # Less verbose

    transformer.to(dtype=torch.bfloat16)
    vae.to(dtype=torch.float16)
    image_encoder.to(dtype=torch.float16)
    text_encoder.to(dtype=torch.float16)
    text_encoder_2.to(dtype=torch.float16)

    vae.requires_grad_(False)
    text_encoder.requires_grad_(False)
    text_encoder_2.requires_grad_(False)
    image_encoder.requires_grad_(False)
    transformer.requires_grad_(False)

    if not high_vram:
        print("Installing dynamic swap for low VRAM...")
        DynamicSwapInstaller.install_model(transformer, device=gpu)
        DynamicSwapInstaller.install_model(text_encoder, device=gpu)
    else:
        print("Moving models to GPU (High VRAM)...")
        text_encoder.to(gpu)
        text_encoder_2.to(gpu)
        image_encoder.to(gpu)
        vae.to(gpu)
        transformer.to(gpu)

    os.makedirs(args.output_dir, exist_ok=True)

    # Load Input Image
    print(f"Loading input image from: {args.image_path}")
    try:
        input_image = Image.open(args.image_path).convert('RGB')
    except FileNotFoundError:
        print(f"Error: Input image not found at {args.image_path}")
        exit(1)
    except Exception as e:
        print(f"Error loading image: {e}")
        exit(1)

    # Run Worker
    final_video_path = worker(
        input_image=input_image,
        prompt=args.prompt,
        n_prompt=args.n_prompt,
        seed=args.seed,
        total_second_length=args.total_second_length,
        latent_window_size=args.latent_window_size,
        steps=args.steps,
        cfg=args.cfg,
        gs=args.gs,
        rs=args.rs,
        gpu_memory_preservation=args.gpu_memory_preservation,
        use_teacache=args.use_teacache,
        outputs_folder=args.output_dir,
        high_vram=high_vram,
        text_encoder=text_encoder,
        text_encoder_2=text_encoder_2,
        tokenizer=tokenizer,
        tokenizer_2=tokenizer_2,
        vae=vae,
        feature_extractor=feature_extractor,
        image_encoder=image_encoder,
        transformer=transformer
    )

    if final_video_path:
        print(f"\nSuccessfully generated video: {final_video_path}")
    else:
        print("\nVideo generation failed.")

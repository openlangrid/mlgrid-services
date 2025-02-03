from diffusers.utils import export_to_video
import tqdm
from torchvision.transforms import ToPILImage
import torch
from transformers import AutoTokenizer, AutoModelForCausalLM
from diffusers import CogVideoXTransformer3DModel, AutoencoderKLCogVideoX

import os
os.environ["TOKENIZERS_PARALLELISM"] = "false"

#prompt="チューリップや菜の花、色とりどりの花が果てしなく続く畑を埋め尽くし、まるでパッチワークのようにカラフルに彩る。朝の柔らかな光が花びらを透かし、淡いグラデーションが映える。風に揺れる花々をスローモーションで捉え、花びらが優雅に舞う姿を映画のような演出で撮影。背景には遠くに連なる山並みや青い空、浮かぶ白い雲が立体感を引き立てる。"
#prompt="静かな森の中を、やわらかな朝陽が差し込む。木漏れ日に照らされた小川には小さな魚が泳ぎ、森の奥からは小鳥のさえずりが聞こえる。少し幻想的な雰囲気を持ちながらも穏やかな情景を映し出す動画。"
#prompt="緑の中を走り抜けてく真っ赤なポルシェ"
#prompt="静かな森の中を走り抜ける赤いポルシェ"
prompt="月面で馬に乗る宇宙飛行士"
device="cuda"
shape=(1,48//4,16,256//8,256//8)
sample_N=25
torch_dtype=torch.bfloat16
eps=1
cfg=2.5

tokenizer = AutoTokenizer.from_pretrained(
    "llm-jp/llm-jp-3-1.8b"
)

text_encoder = AutoModelForCausalLM.from_pretrained(
    "llm-jp/llm-jp-3-1.8b",
    torch_dtype=torch_dtype
)
text_encoder=text_encoder.to(device)

text_inputs = tokenizer(
    prompt,
    padding="max_length",
    max_length=512,
    truncation=True,
    add_special_tokens=True,
    return_tensors="pt",
)
text_input_ids = text_inputs.input_ids
prompt_embeds = text_encoder(text_input_ids.to(device), output_hidden_states=True, attention_mask=text_inputs.attention_mask.to(device)).hidden_states[-1]
prompt_embeds = prompt_embeds.to(dtype=torch_dtype, device=device)

null_text_inputs = tokenizer(
    "",
    padding="max_length",
    max_length=512,
    truncation=True,
    add_special_tokens=True,
    return_tensors="pt",
)
null_text_input_ids = null_text_inputs.input_ids
null_prompt_embeds = text_encoder(null_text_input_ids.to(device), output_hidden_states=True, attention_mask=null_text_inputs.attention_mask.to(device)).hidden_states[-1]
null_prompt_embeds = null_prompt_embeds.to(dtype=torch_dtype, device=device)

# Free VRAM
del text_encoder

transformer = CogVideoXTransformer3DModel.from_pretrained(
    "aidealab/AIdeaLab-VideoJP",
    torch_dtype=torch_dtype
)
transformer=transformer.to(device)

vae = AutoencoderKLCogVideoX.from_pretrained(
    "THUDM/CogVideoX-2b",
    subfolder="vae"
)
vae=vae.to(dtype=torch_dtype, device=device)
vae.enable_slicing()
vae.enable_tiling()

# euler discreate sampler with cfg
z0 = torch.randn(shape, device=device)
latents = z0.detach().clone().to(torch_dtype)

dt = 1.0 / sample_N
with torch.no_grad():
    for i in tqdm.tqdm(range(sample_N)):
        num_t = i / sample_N
        t = torch.ones(shape[0], device=device) * num_t
        psudo_t=(1000-eps)*(1-t)+eps
        positive_conditional = transformer(hidden_states=latents, timestep=psudo_t, encoder_hidden_states=prompt_embeds, image_rotary_emb=None)
        null_conditional = transformer(hidden_states=latents, timestep=psudo_t, encoder_hidden_states=null_prompt_embeds, image_rotary_emb=None)
        pred = null_conditional.sample+cfg*(positive_conditional.sample-null_conditional.sample)
        latents = latents.detach().clone() + dt * pred.detach().clone()

    # Free VRAM
    del transformer

    latents = latents / vae.config.scaling_factor
    latents = latents.permute(0, 2, 1, 3, 4) # [B, F, C, H, W]
    x=vae.decode(latents).sample
    x = x / 2 + 0.5
    x = x.clamp(0,1)
    x=x.permute(0, 2, 1, 3, 4).to(torch.float32)# [B, F, C, H, W]
    print(x.shape)
    x=[ToPILImage()(frame) for frame in x[0]]

export_to_video(x,"./sample/videojp/output.mp4",fps=24)

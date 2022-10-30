# get env var
import os

TOKEN = os.environ['MUBERT_TOKEN']


# parse args
import argparse, os
parser = argparse.ArgumentParser()
parser.add_argument("prompt", nargs="?", type=str, default="sunset over a lake in the mountains")
parser.add_argument("outPathPrefix",type=str, default="out")
args = parser.parse_args()


#@title **Define Mubert methods and pre-compute things**
import numpy as np
from sentence_transformers import SentenceTransformer
minilm = SentenceTransformer('all-MiniLM-L6-v2')

mubert_tags_string = 'tribal,action,kids,neo-classic,run 130,pumped,jazz / funk,ethnic,dubtechno,reggae,acid jazz,liquidfunk,funk,witch house,tech house,underground,artists,mystical,disco,sensorium,r&b,agender,psychedelic trance / psytrance,peaceful,run 140,piano,run 160,setting,meditation,christmas,ambient,horror,cinematic,electro house,idm,bass,minimal,underscore,drums,glitchy,beautiful,technology,tribal house,country pop,jazz & funk,documentary,space,classical,valentines,chillstep,experimental,trap,new jack swing,drama,post-rock,tense,corporate,neutral,happy,analog,funky,spiritual,sberzvuk special,chill hop,dramatic,catchy,holidays,fitness 90,optimistic,orchestra,acid techno,energizing,romantic,minimal house,breaks,hyper pop,warm up,dreamy,dark,urban,microfunk,dub,nu disco,vogue,keys,hardcore,aggressive,indie,electro funk,beauty,relaxing,trance,pop,hiphop,soft,acoustic,chillrave / ethno-house,deep techno,angry,dance,fun,dubstep,tropical,latin pop,heroic,world music,inspirational,uplifting,atmosphere,art,epic,advertising,chillout,scary,spooky,slow ballad,saxophone,summer,erotic,jazzy,energy 100,kara mar,xmas,atmospheric,indie pop,hip-hop,yoga,reggaeton,lounge,travel,running,folk,chillrave & ethno-house,detective,darkambient,chill,fantasy,minimal techno,special,night,tropical house,downtempo,lullaby,meditative,upbeat,glitch hop,fitness,neurofunk,sexual,indie rock,future pop,jazz,cyberpunk,melancholic,happy hardcore,family / kids,synths,electric guitar,comedy,psychedelic trance & psytrance,edm,psychedelic rock,calm,zen,bells,podcast,melodic house,ethnic percussion,nature,heavy,bassline,indie dance,techno,drumnbass,synth pop,vaporwave,sad,8-bit,chillgressive,deep,orchestral,futuristic,hardtechno,nostalgic,big room,sci-fi,tutorial,joyful,pads,minimal 170,drill,ethnic 108,amusing,sleepy ambient,psychill,italo disco,lofi,house,acoustic guitar,bassline house,rock,k-pop,synthwave,deep house,electronica,gabber,nightlife,sport & fitness,road trip,celebration,electro,disco house,electronic'
mubert_tags = np.array(mubert_tags_string.split(','))
mubert_tags_embeddings = minilm.encode(mubert_tags)

from IPython.display import Audio, display
import httpx
import json

def get_track_by_tags(tags, pat, duration, maxit=20, autoplay=False, loop=False):
  if loop:
    mode = "loop"
  else:
    mode = "track"
  r = httpx.post('https://api-b2b.mubert.com/v2/RecordTrackTTM', 
      json={
          "method":"RecordTrackTTM",
          "params": {
              "pat": TOKEN, 
              "duration": duration,
              "tags": tags,
              "mode": mode
          }
      })

  rdata = json.loads(r.text)
  assert rdata['status'] == 1, rdata['error']['text']
  trackurl = rdata['data']['tasks'][0]['download_link']

  print('Generating track ', end='')
  for i in range(maxit):
      r = httpx.get(trackurl)
      if r.status_code == 200:
          display(Audio(trackurl, autoplay=autoplay))
          break
      time.sleep(1)
      print('.', end='')

def find_similar(em, embeddings, method='cosine'):
    scores = []
    for ref in embeddings:
        if method == 'cosine': 
            scores.append(1 - np.dot(ref, em)/(np.linalg.norm(ref)*np.linalg.norm(em)))
        if method == 'norm': 
            scores.append(np.linalg.norm(ref - em))
    return np.array(scores), np.argsort(scores)

def get_tags_for_prompts(prompts, top_n=3, debug=False):
    prompts_embeddings = minilm.encode(prompts)
    ret = []
    for i, pe in enumerate(prompts_embeddings):
        scores, idxs = find_similar(pe, mubert_tags_embeddings)
        top_tags = mubert_tags[idxs[:top_n]]
        top_prob = 1 - scores[idxs[:top_n]]
        if debug:
            print(f"Prompt: {prompts[i]}\nTags: {', '.join(top_tags)}\nScores: {top_prob}\n\n\n")
        ret.append((prompts[i], list(top_tags)))
    return ret


prompt = 'vladimir lenin smoking weed with bob marley' #@param {type:"string"}
duration = 30 #@param {type:"number"}
loop = False #@param {type:"boolean"}

def generate_track_by_prompt(prompt, duration, loop=False):
  _, tags = get_tags_for_prompts([prompt,])[0]
  try:
    get_track_by_tags(tags, TOKEN, duration, autoplay=True, loop=loop)
  except Exception as e:
    print(str(e))
  print('\n')

generate_track_by_prompt(prompt, duration, loop)





# construct opts
import torch
modelPath = args.modelPath
outPathPrefix = args.outPathPrefix
modelOpts = {
    "revision": args.modelRevision,
    "torch_dtype": torch.float16
}
pipeOpts = {
    "prompt": args.prompt,
    "nagetive_prompt": args.negativePrompt,
    "init_image": args.initImage
}

# adjust options before save
if modelPath == "naclbit/trinart_stable_diffusion_v2" and modelOpts["revision"] == "fp16":
    modelOpts["revision"] = "diffusers-60k"
elif modelPath == "doohickey/trinart-waifu-diffusion-50-50" or modelPath == "hakurei/waifu-diffusion" or modelPath == "sd-dreambooth-library/disco-diffusion-style":
    del modelOpts["revision"]
    del modelOpts["torch_dtype"]

# write options
import json
class Encoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, torch.dtype):
            return str(obj)
        return json.JSONEncoder.default(self, obj)
with open(f"{outPathPrefix}.modelOpts.txt", 'w', encoding='UTF-8') as f:
    modelOpts["modelPath"] = modelPath
    f.write(json.dumps(modelOpts, indent=2, cls=Encoder))
with open(f"{outPathPrefix}.pipeOpts.txt", 'w', encoding='UTF-8') as f:
    f.write(json.dumps(pipeOpts, indent=2, cls=Encoder))

# adjust options after save
from PIL import Image
modelOpts["use_auth_token"] = TOKEN
pipeOpts["prompt"] = [args.prompt] * args.numOfSamples
pipeOpts["init_image"] = Image.open(pipeOpts["init_image"]) if pipeOpts["init_image"] != None else None


# prepare model
from diffusers import StableDiffusionPipeline, StableDiffusionImg2ImgPipeline
from torch import autocast

if pipeOpts["init_image"]:
    pipe = StableDiffusionImg2ImgPipeline.from_pretrained(
        modelPath, **modelOpts)
else:
    pipe = StableDiffusionPipeline.from_pretrained(
        modelPath, **modelOpts)
#pipe.enable_attention_slicing()
pipe.to("cuda")


# run
with autocast("cuda"):
    images = pipe(**pipeOpts).images


# save images
for i in range(len(pipeOpts["prompt"])):
    image = images[i]
    image.save(f"{outPathPrefix}_{i}.png")


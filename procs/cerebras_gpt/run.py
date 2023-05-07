# Program: VTSTech-GPT.py 2023-03-31 1:12:19 AM
# Description: Python script that generates text with Cerebras GPT pretrained and Corianas finetuned models 
# Author: Written by Veritas//VTSTech (veritas@vts-tech.org)
# GitHub: https://github.com/Veritas83
# Homepage: www.VTS-Tech.org
# Dependencies: transformers, colorama, Flask, torch
# pip install transformers colorama Flask torch
# Models are stored at C:\Users\%username%\.cache\huggingface\hub
import argparse
import time
import random
from transformers import AutoTokenizer, AutoModelForCausalLM, pipeline
from colorama import Fore, Back, Style, init

global start_time, end_time, build, model_size, model_name, prompt_text
init(autoreset=True)
build="v0.2-r07"
tok=random.seed()
eos_token_id=tok
model_size = "111m"
model_name = "cerebras/Cerebras-GPT-111M"
parser = argparse.ArgumentParser(description='Generate text with Cerebras GPT models')
parser.add_argument('-m', '--modelSize', choices=['111m', '256m', '590m','1.3b','2.7b','6.7b','13b'], help='Choose the model size to use (default: 111m)', type=str.lower)
parser.add_argument('-ce', '--cerb', action='store_true', help='Use Cerebras GPT pretrained models (default)')
parser.add_argument('-co', '--cori', action='store_true', help='Use Corianas finetuned models')
parser.add_argument('-cu', '--model', type=str, help='Specify a custom model')
parser.add_argument('-p', '--prompt', type=str, default="AI is", help='Text prompt to generate from (default: "AI is")')
parser.add_argument('-s', '--size', type=int, default=256)
parser.add_argument('-l', '--length', type=int, default=256)
parser.add_argument('-tk', '--topk', type=float, default=40)
parser.add_argument('-tp', '--topp', type=float, default=0.9)
parser.add_argument('-ty', '--typp', type=float, default=None)
parser.add_argument('-tm', '--temp', type=float, default=0.7)
parser.add_argument('-t', '--time', action='store_true', help='Print execution time')
parser.add_argument('-c', '--cmdline', action='store_true', help='cmdline mode, no webserver')
parser.add_argument("--inputPath", nargs="?", type=str, default="input.txt")
parser.add_argument("--inputLanguage", type=str, default="en")
parser.add_argument("--outputPath",type=str, default="input")
args = parser.parse_args()
if args.model:
	model_size = args.model
top_p = args.topp
top_k = args.topk
typ_p = args.typp
temp = args.temp

def get_model():
    return args.model	    	

model_name = get_model()
max_length = int(args.length)

def banner(prompt):
    global model_name
    print(Style.BRIGHT + f"VTSTech-GPT {build} - www: VTS-Tech.org git: Veritas83")
    print("Using Model : " + Fore.RED + f"{model_name}")
    print("Using Prompt: " + Fore.YELLOW + f"{prompt}")
    print("Using Params: " + Fore.YELLOW + f"max_new_tokens:{max_length} do_sample:True use_cache:True no_repeat_ngram_size:2 top_k:{top_k} top_p:{top_p} typical_p:{typ_p} temp:{temp}")

def CerbGPT(prompt_text):
    global start_time, end_time, build, model_size, model_name	
    temp=None
    top_k=None
    top_p=None
    start_time = time.time()
    #model_name = get_model()
    try:
        tokenizer = AutoTokenizer.from_pretrained(model_name)
    except:
        from transformers import LlamaTokenizer
        tokenizer = LlamaTokenizer.from_pretrained(model_name)
    try:
        model = AutoModelForCausalLM.from_pretrained(model_name)
    except:
        from transformers import LlamaForCausalLM
        model = LlamaForCausalLM.from_pretrained(model_name)
    opts = {}
    if temp is not None:
        opts["temperature"] = temp
    if top_k is not None:
        opts["top_k"] = top_k
    if top_p is not None:
        opts["top_p"] = top_p
    if typ_p is not None:
        opts["typical_p"] = typ_p
    pipe = pipeline("text-generation", model=model, tokenizer=tokenizer, device=0)
#    prompt_text = f"""Below is an instruction that describes a task. Write a response that appropriately completes the request.
#### Instruction:
#{prompt_text}
#### Response:"""
    generated_text = pipe(prompt_text, max_new_tokens=max_length, do_sample=True, use_cache=True, no_repeat_ngram_size=2, **opts)[0]
    
    end_time = time.time()
    return generated_text['generated_text']

# python run.py -cu cerebras/Cerebras-GPT-2.7B -c -t -p "アルパカについて教えてください。"
# python run.py -cu cerebras/Cerebras-GPT-2.7B -c -t -p "Tell me about alpacas."
# python run.py -cu cerebras/Cerebras-GPT-6.7B -c -t -p "アルパカについて教えてください。"
# python run.py -cu cerebras/Cerebras-GPT-13B -c -t -p "Tell me about alpacas."
# 

def loadFile(path):
    import os
    if path and os.path.exists(path):
        with open(path) as f:
            return f.read()
    return None


if __name__ == '__main__':
    global start_time, end_time	    
    input = loadFile(args.inputPath)
    banner(input)
    result = CerbGPT(input)
    with open(args.outputPath, 'w', encoding='UTF-8') as f:
        f.write(result)
    print(result)

    if args.time:
        print(Style.BRIGHT + Fore.RED + f"Script finished. Execution time: {end_time - start_time:.2f} seconds")

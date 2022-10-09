import argparse, os

parser = argparse.ArgumentParser()
parser.add_argument("arg1", nargs="?", type=str, default="argvalue1")
parser.add_argument("--optOpt", type=str)
args = parser.parse_args()
print(f"{args.arg1}")
print(f"{args.optOpt}")
if not args.optOpt:
    print("NONE")
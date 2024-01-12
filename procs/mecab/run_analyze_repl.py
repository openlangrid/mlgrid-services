def run():
    import MeCab
    mecab = MeCab.Tagger()
    print("ready", flush=True)

    import json, sys
    for line in sys.stdin:
        input = json.loads(line)
        with open(input["textPath"]) as f:
            text = f.read()
        language = input["textLanguage"]
        outputPath = input["outputPath"]
        ret = mecab.parse(text)
        morphs = []
        for line in ret.split("\n"):
            if line == "EOS":
                break
            wordAndAttrs = line.split('\t')
            pos1, pos2, _, _, _, _, lemma, *_ = wordAndAttrs[1].split(",")
            morphs.append({
                "word": wordAndAttrs[0],
                "pos": pos1,
                "posDetail": pos2,
                "lemma": lemma})
        ret = json.dumps(morphs, indent=2, ensure_ascii=False)
        with open(outputPath, mode="w") as f:
            f.write(ret)
        print(f"ok", flush=True)


def main():
    run()


if __name__ == "__main__": 
    main()

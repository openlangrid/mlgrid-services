import MeCab
mecab = MeCab.Tagger()

import json
ret = mecab.parse(
    "Amazon Web Services（アマゾン ウェブ サービス、略称：AWS）とは、https://Amazon.comにより提供されているクラウドコンピューティングサービスである。")
morphs = []
for line in ret.split("\n"):
    if line == "EOS":
        break
    wordAndAttrs = line.split('\t')
    pos1, pos2, _, _, _, _, lemma, *_ = wordAndAttrs[1].split(",")
    morphs.append({
        "word": wordAndAttrs[0],
        "pos": pos1 if pos2 != "名詞" else pos2,
        "lemma": lemma})
print(json.dumps(morphs, indent=2, ensure_ascii=False))

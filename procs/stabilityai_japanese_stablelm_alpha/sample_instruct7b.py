import torch
from transformers import LlamaTokenizer, AutoModelForCausalLM

tokenizer = LlamaTokenizer.from_pretrained(
    "novelai/nerdstash-tokenizer-v1",
    additional_special_tokens=['▁▁'])
model = AutoModelForCausalLM.from_pretrained(
    "stabilityai/japanese-stablelm-instruct-alpha-7b",    
    trust_remote_code=True,
    torch_dtype=torch.float16,
    device_map="auto")
model.eval()

def build_prompt(user_query, inputs="", sep="\n\n### "):
    sys_msg = "以下は、タスクを説明する指示と、文脈のある入力の組み合わせです。要求を適切に満たす応答を書きなさい。"
    p = sys_msg
    roles = ["指示", "応答"]
    msgs = [": \n" + user_query, ": "]
    if inputs:
        roles.insert(1, "入力")
        msgs.insert(1, ": \n" + inputs)
    for role, msg in zip(roles, msgs):
        p += sep + role + msg
    return p

# this is for reproducibility.
# feel free to change to get different result
#seed = 42
#torch.manual_seed(seed)

q="""次の文章を箇条書きで要約してください。新型 コロナウイルスや ギア スクール 構想の影響によって人に1台の情報端末を使って教育学習をすることが当たり前のようになってきていますで そのような中 教材 へのアクセス履歴ですとかビデオの視聴履歴 それから 小テストなどへの回答 履歴 様々な 教育 データが自然と蓄積されつつあります 岐阜 システムを開発するにあたり私たちが目指したことは これを科学的に分析して一人一人の教員や 学生さんにあった形で支援をしたいということであります これまでの買い上げ 経験に基づく教育をデータや エビデンスに基づく教育へと変革をしたいと考えています
けどもやっぱり 事業改善ですで我々 教師の仕事はやっぱ ほとんどが事業に関わってるっていう所です 生徒たちに提供するまあ 言われるサービスは事業のクオリティを上げていくということです いわゆる主体的で対話的で深い学び っていうのは 今年 学習指導でも言われてるとこですけどその中に ICT の活用が不可欠ではないか 私は考えていますですから まず事業を改善するためには どうしなければならないか このリーフシステムを存分に活用していきながら事業を変えていく 改善していくというところです
 リーフシステムの機能の一つが デジタル教材配信システムブックロールです
ブックロールとは 教員が教科書や 教材 音声ファイルを登録することで デジタル配信できるシステムです
学生はデジタル端末を使用し いつでもどこでも閲覧できます またブックロール上での学生の行動は学習ログとしてデータベースに記録されます
ブックロールは対面授業でもオンライン授業でも 教材の配信に利用でき さらに得られた エビデンス データから授業改善に役立てることができます
リーディングの授業の時なんですけれども 教科書を読んでいく 授業で してその時に ただ ただ 教科書を読むだけじゃなくて その教科書の本文を PDF 化してブックロールに上げて 生徒が自分のわからない単語は黄色だったりとか大事だと思うところはあったっていう風にこう 線を引いて我々がどういう風に生徒が読んでいるかっていうのを理解するのに使っていますね"""

# Infer with prompt without any additional input
user_inputs = {
    "user_query": q, #"VR とはどのようなものですか？",
    "inputs": ""
}
prompt = build_prompt(**user_inputs)
print(prompt)

input_ids = tokenizer.encode(
    prompt, 
    add_special_tokens=False, 
    return_tensors="pt"
)

tokens = model.generate(
    input_ids.to(device=model.device),
    max_new_tokens=256,
    temperature=1,
    top_p=0.95,
    do_sample=True,
)[0]
tokens = tokens[input_ids.shape[1]:]

out = tokenizer.decode(tokens, skip_special_tokens=True).strip()
print(out)
"""バーチャルリアリティは、現実の世界のように見える仮想世界の 3D 仮想現実のシミュレーションです。これは、ヘッドセットを介して、ユーザーが見たり、聞いたり、体験できるものです。"""

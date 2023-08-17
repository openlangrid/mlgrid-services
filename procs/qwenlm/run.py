def run(tokenizer_model_name: str, model_name: str, text: str):
    from transformers import AutoModelForCausalLM, AutoTokenizer
    from transformers.generation import GenerationConfig

    if not tokenizer_model_name:
        tokenizer_model_name = model_name

    tokenizer = AutoTokenizer.from_pretrained(
        tokenizer_model_name, trust_remote_code=True)

    # bf16 を使用
    # model = AutoModelForCausalLM.from_pretrained("Qwen/Qwen-7B-Chat", device_map="auto", trust_remote_code=True, bf16=True).eval()
    # fp16 を使用
    model = AutoModelForCausalLM.from_pretrained(
        model_name, device_map="auto", trust_remote_code=True, fp16=True).eval()
    # CPU のみ使用
    # model = AutoModelForCausalLM.from_pretrained("Qwen/Qwen-7B-Chat", device_map="cpu", trust_remote_code=True).eval()
    # オートモードを使用すると、デバイスに応じて自動的に精度が選択されます。
    # model = AutoModelForCausalLM.from_pretrained(
    #     model, device_map="auto", trust_remote_code=True).eval()

    # 生成のためのハイパーパラメータを指定
    model.generation_config = GenerationConfig.from_pretrained(
        model_name, trust_remote_code=True)

    # 第一轮对话 第一回対話ターン
    response, history = model.chat(tokenizer, text, history=None)
    return response
    # こんにちは！ お役に立ててうれしいです。

    # 第二轮对话 第二回対話ターン
#    response, history = model.chat(tokenizer, "给我讲一个年轻人奋斗创业最终取得成功的故事。", history=history)
#    print(response)
    # これは、自分のビジネスを始めようと奮闘し、やがて成功する若者の物語である。
    # この物語の主人公は、平凡な家庭に生まれ、平凡な労働者である両親を持つ李明である。 李明は子供の頃から起業家として成功することを目標としていた。
    # この目標を達成するため、李明は猛勉強して大学に入った。 大学時代には、さまざまな起業家コンテストに積極的に参加し、多くの賞を獲得した。 また、余暇を利用してインターンシップにも参加し、貴重な経験を積んだ。
    # 卒業後、李明は起業を決意した。 投資先を探し始めたが、何度も断られた。 しかし、彼はあきらめなかった。 彼は懸命に働き続け、ビジネスプランを改善し、新たな投資機会を探した。
    # やがて李明は投資を受けることに成功し、自分のビジネスを始めた。 彼は新しいタイプのソフトウェアの開発に焦点を当てたテクノロジー会社を設立した。 彼のリーダーシップの下、会社は急速に成長し、テクノロジー企業として成功を収めた。
    # 李明の成功は偶然ではない。 彼は勤勉で、たくましく、冒険好きで、常に学び、自分を高めている。 彼の成功はまた、努力すれば誰でも成功できることを証明している。

    # 第三轮对话 第三回対話ターン
#    response, history = model.chat(tokenizer, "给这个故事起一个标题", history=history)
#    print(response)


def main(tokenizer_model: str, model: str, inputPath: str, inputLanguage: str, outputPath: str):
    with open(inputPath) as f:
        text = f.read()
    ret = run(tokenizer_model, model, text)
    with open(outputPath, mode="w") as f:
        f.write(str(ret))


if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--tokenizer_model", type=str, default=None)
    parser.add_argument("--model", type=str, default="Qwen/Qwen-7B-Chat")
    parser.add_argument("--inputPath", type=str, default="./sample/input.txt")
    parser.add_argument("--inputLanguage", type=str, default="ja")
    parser.add_argument("--outputPath", type=str, default="./sample/output.txt")
    main(**vars(parser.parse_args()))

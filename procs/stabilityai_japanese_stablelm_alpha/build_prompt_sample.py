def build_prompt(user_query, inputs="", sep="\n\n### "):
    sys_msg = "以下は、タスクを説明する指示と、文脈のある入力の組み合わせです。要求を適切に満たす応答を書きなさい。"
    p = sys_msg
    roles = ["指示", "応答"]
    msgs = [": \n" + user_query, ": \n"]
    if inputs:
        roles.insert(1, "入力")
        msgs.insert(1, ": \n" + inputs)
    for role, msg in zip(roles, msgs):
        p += sep + role + msg
    return p

print(build_prompt("新しいお菓子を考えてください。"))

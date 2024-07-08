from argparse import ArgumentParser
from pathlib import Path

def run(speakerId: int, dictDir: Path) -> None:
    print(f"speakerId: {speakerId}, dictDir: {dictDir}")
    from voicevox_core import VoicevoxCore
    core = VoicevoxCore(
        acceleration_mode="AUTO",
        open_jtalk_dict_dir=dictDir
    )
    core.load_model(speakerId)
    print("ready", flush=True)

    try:
        import json, sys
        for line in sys.stdin:
            input = json.loads(line)
            with open("/work/" + input["textPath"]) as f:
                text = f.read()
            language = input["textLanguage"]
            outputPathPrefix = "/work/" + input["outputPathPrefix"]
            audio_query = core.audio_query(text, speakerId)
            wav = core.synthesis(audio_query, speakerId)
            with open(outputPathPrefix + ".wav", "wb") as out:
                out.write(wav)
            print(f"ok", flush=True)
    except RuntimeError:  # RuntimeError: "addmm_impl_cpu_" not implemented for 'Half'
        print("ng RuntimeError", flush=True)

if __name__ == "__main__": 
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("speakerId", type=int, nargs="?", default="0")
    parser.add_argument("dictDir", type=Path, nargs="?", default="./open_jtalk_dic_utf_8-1.11")
    run(**vars(parser.parse_args()))

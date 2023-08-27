from utils.generation import SAMPLE_RATE, generate_audio, preload_models
from scipy.io.wavfile import write as write_wav
from IPython.display import Audio

# download and load all models
preload_models()

# generate audio from text
text_prompt = """
Hello, my name is Nose. And uh, and I like hamburger. Hahaha... But I also have other interests such as playing tactic toast.
"""
#audio_array = generate_audio(text_prompt)

# save audio to disk
#write_wav("./sample/vallex_generation.wav", SAMPLE_RATE, audio_array)

write_wav("./sample/japanese.wav", SAMPLE_RATE,
          generate_audio("今日もいい天気ですね"))

# play text in notebook
#Audio(audio_array, rate=SAMPLE_RATE)

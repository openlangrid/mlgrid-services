import asyncio
import websockets
import sys
import wave

async def run_test(uri, file):
    async with websockets.connect(uri) as websocket:
        wf = wave.open(file, "rb")
        count = 0
        print(f"channels: {wf.getnchannels()}")
        print(f"sampwidth: {wf.getsampwidth()}")
        print(f"framerate: {wf.getframerate()}")
        await websocket.send('{ "config" : { "sample_rate" : %d } }' % (wf.getframerate()))
        buffer_size = int(wf.getframerate() * 0.2) # 0.2 seconds of audio
        while True:
            data = wf.readframes(buffer_size)
            if len(data) == 0:
                break
            await websocket.send(data)
            count = count + 1
            print(count)
            print(await websocket.recv())

        await websocket.send('{"eof" : 1}')
        count = count + 1
        print(count)
        print (await websocket.recv())

url = sys.argv[1] if len(sys.argv) > 1 else "ws://localhost:2700"
file = sys.argv[2] if len(sys.argv) > 2 else "test.wav"

asyncio.run(run_test(url, file))
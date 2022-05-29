class Recorder{
	constructor(){
		this.sampleRate = 16000;
        this.recording = false;
	}
	getAudioContext(){
		return this.audioContext;
	}

	isRecording(){
		return this.recording;
	}

	onStartRecording(stream, autioContext){}

	/**
	 * 
	 * @param {Float32Array} channelData 
	 */
    onProcessRecording(channelData){}

	onStopRecording(){}

	start(sampleRate){
		if(sampleRate) this.sampleRate = sampleRate;
		this.recording = true;
		navigator.mediaDevices
			.getUserMedia({audio: true, video: false})
			.then(stream => {
				window.AudioContext = window.AudioContext || window.webkitAudioContext;
				this.audioContext = new window.AudioContext({
					sampleRate: this.sampleRate,
				});
				this.stream = stream;
                this.onStartRecording(stream, this.audioContext);
				const sp = this.audioContext.createScriptProcessor(1024, 1, 1);
				sp.onaudioprocess = e=>{
					if(this.recording) this.onProcessRecording(e.inputBuffer.getChannelData(0));
				};
				this.audioContext.createMediaStreamSource(stream).connect(sp);
				sp.connect(this.audioContext.destination);
				console.debug(`recording started. required sample rate: ${sampleRate}, `
					+ `actual sample rate: ${this.audioContext.sampleRate}`);
/*
				window.SpeechRecognition = window.webkitSpeechRecognition || window.SpeechRecognition;
				if(window.SpeechRecognition){
					this.sr = new window.SpeechRecognition();
					this.sr.continuous = true;
					this.sr.lang = 'ja-JP';
					this.sr.interimResults = false;
					this.sr.maxAlternatives = 1;
					this.sr.onresult = e=>{
						this.ws.send(JSON.stringify({
							"type": "asrResult",
							"body": e.results[e.results.length - 1][0].transcript
						}));
					};
					this.sr.onend = e=>{
						console.log("asr stopped.")
						this.sr = null;
						if(this.recording){
							this.finish();
						} else{
							this.startClosing();
						}
					}
					this.sr.start();
				}
*/
			});
	}
	stop() {
		if(!this.recording) return;
		this.recording = false;
		if(this.stream){
			this.stream.getTracks().forEach(t=>t.stop());
		}
		console.debug("recording stopped.");
        this.onStopRecording();
/*		if(this.sr){
			this.sr.stop();
		} else{
			this.startClosing();
		}
*/
    }
	startClosing(){
/*		if(this.onending) this.onending();
		this.ws.send(JSON.stringify({"type": "recordingFinished"}));
		if(this.audioContext){
			this.audioContext.close().then(()=>{
				console.log("audioContext closed.");
				this.audioContext = null;
			});
		}
*/
   	}
}
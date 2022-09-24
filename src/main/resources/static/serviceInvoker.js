// 各サービス呼び出しクラス用のベースクラス
// サービス呼び出し自体はServiceInvokerクラスのinvokeメソッドでも行えるが、
// どんなサービスがあるか、そのサービスにどんなメソッドがあるかを明示するために、
// このクラスと各サービス呼び出し用のクラスを作っている。
class Service{
    constructor(serviceInvoker, serviceId){
        this.serviceInvoker = serviceInvoker;
        this.serviceId = serviceId;
    }
    invoke(methodName, args){
        return this.serviceInvoker.invoke(
            this.serviceId, methodName, args);
    }
}

// 各サービス呼び出しクラス。サービスの種類毎に用意する。
// サービス毎にどんなメソッド名があるかを明示するために設けている。
class ContinuousSpeechRecognitionService extends Service{
    /**
     * @param {string} language 
     * @param {{channels: number; sampleSizeInBits: number, frameRate: number}} config 
     * @returns {Promise<{sentenceId: number; sentence: string; fixed: boolean; accuracy: number}[]>}
     */
	startRecognition(language, config){
        return this.invoke("startRecognition", Array.prototype.slice.call(arguments));
    }
	processRecognition(sessionId, audio){
        return this.invoke("processRecognition", Array.prototype.slice.call(arguments));
    }
	stopRecognition(sessionId){
        return this.invoke("stopRecognition", Array.prototype.slice.call(arguments));
    }
}
class SpeechRecognition extends Service{
	recognize(language, format, audio){
		return this.invoke("recognize", Array.prototype.slice.call(arguments));
	}
}
class ImageClassificationService extends Service{
    classify(format, image, labelLang, maxResults){
		return this.invoke("classify", Array.prototype.slice.call(arguments));
	}
}
class ObjectDetectionService extends Service{
    detect(format, b64Image, labelLang, maxResults){
		return this.invoke("detect", Array.prototype.slice.call(arguments));
	}
}
class HumanPoseEstimationService extends Service{
	estimate(format, image, maxResults){
		return this.invoke("estimate", Array.prototype.slice.call(arguments));
	}
}
class ImageToImageConversionService extends Service{
	convert(format, image){
		return this.invoke("convert", Array.prototype.slice.call(arguments));
	}
}
class SpeechEmotionRecognition extends Service{
	recognize(language, audioFormat, audio){
		return this.invoke("recognize", Array.prototype.slice.call(arguments));
	}
}
class TextImageGenerationService extends Service{
    generate(language, text, imageFormat, maxResults){
        return this.invoke("generate", Array.prototype.slice.call(arguments));
    }
}
class TextGuidedImageConversionService extends Service{
	convert(language, prompt, frmat, image){
		return this.invoke("convert", Array.prototype.slice.call(arguments));
	}
}
class TextSentimentAnalysisService extends Service{
	analyze(language, text){
		return this.invoke("analyze", Array.prototype.slice.call(arguments));
	}
}
class TextToSpeechService extends Service{
	speak(language, text, voiceType, audioType){
        return this.invoke("speak", Array.prototype.slice.call(arguments));
	}
}
class TranslationService extends Service{
	translate(sourceLang, targetLang, source){
        return this.invoke("translate", Array.prototype.slice.call(arguments));
    }
}

// Service呼び出しに使用するクラスのベースクラス。派生クラスで実装するinvokeメソッドと、
// 各サービスクラスを返すメソッドだけを用意する。
class ServiceInvoker{
    invoke(serviceId, methodName, args){}

    /** return {ContinuousSpeechRecognitionService} */
    continuousSpeechRecognition(serviceId){
        return new ContinuousSpeechRecognitionService(this, serviceId);
    }
	speechRecognition(serviceId){
		return new SpeechEmotionRecognition(this, serviceId);
	}
    imageClassification(serviceId){
        return new ImageClassificationService(this, serviceId);
    }
    objectDetection(serviceId){
        return new ObjectDetectionService(this, serviceId);
    }
	humanPoseEstimation(serviceId){
		return new HumanPoseEstimationService(this, serviceId);
	}
	imageToImageConversion(serviceId){
		return new ImageToImageConversionService(this, serviceId);
	}
	speechEmotionRecognition(serviceId){
		return new SpeechEmotionRecognition(this, serviceId);
	}
	translation(serviceId){
		return new TranslationService(this, serviceId);
	}
	textImageGeneration(serviceId){
		return new TextImageGenerationService(this, serviceId);
	}
	textGuidedImageConversion(serviceId){
		return new TextGuidedImageConversionService(this, serviceId);
	}
    textSentimentAnalysis(serviceId){
        return new TextSentimentAnalysisService(this, serviceId);
    }
	textToSpeech(serviceId){
		return new TextToSpeechService(this, serviceId);
	}
} 

// Websocketを使ってサービス呼び出しを行うクラス
class WSServiceInvoker extends ServiceInvoker{
	constructor(url){
        super();
		this.bson = new BSON();
		this.url = url;
		this.ws = null;
		this.sendbuf = [];
		this.rid = 0;
		this.handlers = {};
	}

	invoke(serviceId, method, args){
		return new Promise((resolve, reject)=>{
			const rid = this.rid++;
			const msg = {
				reqId: rid, serviceId: serviceId,
				method: method, args: args
			};
			console.debug("req:", msg);
			const data = this.bson.serialize(msg);
//			const data = JSON.stringify(msg);			
//			console.debug("req(encoded):", data);
			this.send(data);
			this.handlers[rid] = r=>{
				resolve(r);
				delete this.handlers[rid];
			};
		});
	}

	send(data){
		this.prepareWs();
		if(this.ws.readyState == 0){
			this.sendbuf.push(data);
		} else if(this.ws.readyState == 1){
			this.ws.send(data);
		} else{
			console.error("websocket connection closing.");
		}
	}

	prepareWs(){
		if(this.ws != null) return;
		this.ws = new WebSocket(this.url);
		this.ws.binaryType = "arraybuffer";
		this.ws.addEventListener('open', e=>{
			console.debug("websocket connection opened.");
			for(let b of this.sendbuf){
				this.ws.send(b);
			}
			this.sendbuf = [];
		});
		this.ws.addEventListener('close', e=>{
			console.debug("websocket connection closed.");
			this.ws = null;
			this.rid = 0;
			this.handlers = {};
		});
		this.ws.addEventListener("message", e=>{
			if(e.data instanceof ArrayBuffer) {
				// binary
				const array = new Uint8Array(e.data);
//				console.debug("res:", array);
				const r = this.bson.deserialize(Buffer.from(array));
				console.debug("res(decoded):", r);
				this.handlers[r.reqId](r);
			} else {
				// text
//				console.debug("res:", e.data);
				const r = JSON.parse(e.data);
				console.debug("res(decoded):", r);
				this.handlers[r.reqId](r);
			}
		});
	}
}

class HTTPServiceInvoker extends ServiceInvoker{
	constructor(baseUrl){
        super();
		this.baseUrl = baseUrl;
	}

	invoke(serviceId, method, args){
		const body = JSON.stringify({
			method: method,
			args: args
		});
		console.debug("req:", body);
		return new Promise((resolve, reject)=>{
			fetch(`${this.baseUrl}/${serviceId}`, {
				method: "POST", mode: 'cors',
				headers: {'Content-Type': 'application/json'},
				body: body
			})
			.then(r=>{
				const ret = r.json();
				console.debug("res(decoded):", ret);
				resolve(ret);
			})
			.catch(e=>reject(e));
		});
	}	
}

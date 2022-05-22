package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.langrid.mlgridservices.util.WavRecorder;
import org.langrid.service.ml.ContinuousSpeechRecognitionConfig;
import org.langrid.service.ml.ContinuousSpeechRecognitionService;
import org.langrid.service.ml.ContinuousSpeechRecognitionTranscript;

import jp.go.nict.langrid.commons.io.FileUtil;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;

public class VoskSpeechRecognitionService implements ContinuousSpeechRecognitionService{
	private String uri = "ws://localhost:2700";
	public VoskSpeechRecognitionService(){
	}
	public VoskSpeechRecognitionService(String uri){
		this.uri = uri;
	}
	static class Context{
		WebSocket ws;
		int sentenceCount;
		boolean closing;
		Object lock;
		WavRecorder recorder;
		ContinuousSpeechRecognitionTranscript[] result;
		ProcessFailedException exception;
		void notifyResult(ContinuousSpeechRecognitionTranscript... result){
			this.result = result;
			synchronized(lock){
				lock.notify();
			}
		}
		void notifyException(ProcessFailedException exception){
			this.exception = exception;
			synchronized(lock){
				lock.notify();
			}
		}
		void prepareWait(){
			lock = new Object();
			result = null;
			exception = null;
		}
		void waitResultOrException() throws InterruptedException{
			synchronized(lock){
				lock.wait(2000);
			}
		}
	}
	private WebSocketFactory factory = new WebSocketFactory();
	private Map<String, Context> contexts = new ConcurrentHashMap<>();
	private int receiverCount;
	private ObjectMapper mapper = new ObjectMapper();

	@Override
	public synchronized String startRecognition(
		String language, 
		ContinuousSpeechRecognitionConfig config)
		throws ProcessFailedException
	{
		var c = new Context();
		var sessionId = "session_" + receiverCount++;
		contexts.put(sessionId, c);
		try{
			String ds = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date());
			var f = FileUtil.createUniqueFile(new File("./procs/speech_recognition_vosk/temp-" + ds + "-"), "recording");
			c.recorder = new WavRecorder(f.toString(), config.getChannels(), config.getSampleSizeInBits(), config.getFrameRate());
			c.ws = factory.createSocket(this.uri);
			c.ws.addListener(new WebSocketAdapter() {
				@Override
				public void onTextMessage(WebSocket websocket, String message) {
					int sentenceId = c.sentenceCount;
					try{
						var ret = mapper.readValue(message, Map.class);
						if(ret.containsKey("partial")){
							c.notifyResult(
								new ContinuousSpeechRecognitionTranscript(
									sentenceId, ret.get("partial").toString(),
									false, Double.NaN));
						} else if(ret.containsKey("text")){
							int count = 0;
							double confSum = 0;
							@SuppressWarnings("unchecked")
							var result = (List<Map<?, ?>>)ret.get("result");
							if(result != null){
								for(var r : result){
									confSum += ((Number)r.get("conf")).doubleValue();
									count++;
								};
							}
							if(confSum > 0){
								confSum /= count;
							} else{
								confSum = Double.NaN;
							}
							c.notifyResult(new ContinuousSpeechRecognitionTranscript(
								sentenceId, ret.get("text").toString(),
								true, confSum));
							c.sentenceCount++;
							if(c.closing){
								c.ws.sendClose();
								c.ws.disconnect();
								contexts.remove(sessionId);
							}
						} else{
							c.notifyResult(new ContinuousSpeechRecognitionTranscript[]{});
						}
					} catch(RuntimeException e){
						e.printStackTrace();
						c.notifyException(new ProcessFailedException(e));
						c.ws.sendClose();
						c.ws.disconnect();
						contexts.remove(sessionId);
					} catch(JsonProcessingException e){
						c.notifyException(new ProcessFailedException(e));
						c.ws.sendClose();
						c.ws.disconnect();
						contexts.remove(sessionId);
					}
				}

				@Override
				public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
					c.notifyException(new ProcessFailedException(cause));
					c.ws.sendClose();
					c.ws.disconnect();
					contexts.remove(sessionId);
				}
			});
			c.ws.connect();
			c.ws.sendText(String.format("{ \"config\" : { \"sample_rate\" : %d } }", config.getFrameRate()));
		} catch(IOException | WebSocketException e){
			throw new ProcessFailedException(e);
		}
		return sessionId;
	}

	@Override
	public ContinuousSpeechRecognitionTranscript[] processRecognition(String sessionId, byte[] audio)
	throws InvalidParameterException, ProcessFailedException{
		var c = contexts.get(sessionId);
		if(c == null){
			throw new InvalidParameterException("sessionId", "no valid running request: " + sessionId);
		}
		c.prepareWait();
		c.ws.sendBinary(audio);
		try {
			c.recorder.onRawData(audio);
			c.waitResultOrException();
			if(c.result != null) return c.result;
			if(c.exception != null) throw c.exception;
			return new ContinuousSpeechRecognitionTranscript[]{};
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
			c.ws.disconnect();
			contexts.remove(sessionId);
			throw new ProcessFailedException(e);
		}
	}

	@Override
	public ContinuousSpeechRecognitionTranscript[] stopRecognition(String sessionId)
	throws InvalidParameterException, ProcessFailedException{
		var c = contexts.get(sessionId);
		if(c == null){
			throw new InvalidParameterException("rid", "no valid running request: " + sessionId);
		}
		c.prepareWait();
		c.ws.sendText("{\"eof\" : 1}");
		c.closing = true;
		try {
			c.recorder.onRecordingFinished();
			c.waitResultOrException();
			if(c.result != null) return c.result;
			if(c.exception != null) throw c.exception;
			return new ContinuousSpeechRecognitionTranscript[]{};
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
			c.ws.disconnect();
			contexts.remove(sessionId);
			throw new ProcessFailedException(e);
		}
	}
}

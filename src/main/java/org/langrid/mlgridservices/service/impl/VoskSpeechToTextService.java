package org.langrid.mlgridservices.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.langrid.service.ml.ContinuousSpeechRecognitionConfig;
import org.langrid.service.ml.ContinuousSpeechRecognitionReceiverService;
import org.langrid.service.ml.ContinuousSpeechRecognitionService;
import org.langrid.service.ml.ContinuousSpeechRecognitionTranscript;

import jp.go.nict.langrid.commons.util.ArrayUtil;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;

public class VoskSpeechToTextService implements ContinuousSpeechRecognitionService{
	static class Context{
		WebSocket ws;
		int sentenceCount;
		boolean closing;
		ContinuousSpeechRecognitionReceiverService receiver;
	}
	private WebSocketFactory factory = new WebSocketFactory();
	private Map<String, Context> contexts = new HashMap<>();
	private int receiverCount;
	private ObjectMapper mapper = new ObjectMapper();

	@Override
	public synchronized String startRecognition(
		String language, 
		ContinuousSpeechRecognitionConfig config,
		ContinuousSpeechRecognitionReceiverService receiver)
		throws ProcessFailedException
	{
		var c = new Context();
		var rid = "" + receiverCount++;
		contexts.put(rid, c);
		try{
			c.receiver = receiver;
			c.ws = factory.createSocket("ws://localhost:2700");
			c.ws.addListener(new WebSocketAdapter() {
				@Override
				public void onTextMessage(WebSocket websocket, String message) {
					int sid = c.sentenceCount;
					try{
						var ret = mapper.readValue(message, Map.class);
						if(ret.containsKey("partial")){
							c.receiver.onRecognitionResult(rid, ArrayUtil.array(
								new ContinuousSpeechRecognitionTranscript(
									sid, ret.get("partial").toString(),
									false, Double.NaN)));
						} else if(ret.containsKey("text")){
							int count = 0;
							double confSum = 0;
							@SuppressWarnings("unchecked")
							var result = (List<Map<?, ?>>)ret.get("result");
							for(var r : result){
								confSum += ((Number)r.get("conf")).doubleValue();
								count++;
							};
							if(confSum > 0){
								confSum /= count;
							} else{
								confSum = Double.NaN;
							}
							c.receiver.onRecognitionResult(rid, ArrayUtil.array(
								new ContinuousSpeechRecognitionTranscript(
									sid, ret.get("text").toString(),
									true, confSum)));
							c.sentenceCount++;
							if(c.closing){
								c.ws.sendClose();
								c.ws.disconnect();
								contexts.remove(rid);
							}
						} else{
							c.receiver.onRecognitionResult(rid, new ContinuousSpeechRecognitionTranscript[]{});
						}
					} catch(RuntimeException e){
						e.printStackTrace();
						c.receiver.onException(new ProcessFailedException(e));
						c.ws.sendClose();
						c.ws.disconnect();
						contexts.remove(rid);
					} catch(JsonProcessingException e){
						c.receiver.onException(new ProcessFailedException(e));
						c.ws.sendClose();
						c.ws.disconnect();
						contexts.remove(rid);
					}
				}

				@Override
				public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
					c.receiver.onException(new ProcessFailedException(cause));
					c.ws.sendClose();
					c.ws.disconnect();
					contexts.remove(rid);
				}
			});
			c.ws.connect();
			c.ws.sendText(String.format("{ \"config\" : { \"sample_rate\" : %d } }", config.getFrameRate()));
		} catch(IOException | WebSocketException e){
			throw new ProcessFailedException(e);
		}
		return rid;
	}

	@Override
	public void processRecognition(String rid, byte[] audio) throws InvalidParameterException{
		var c = contexts.get(rid);
		if(c == null){
			throw new InvalidParameterException("rid", "no valid running request: " + rid);
		}
		c.ws.sendBinary(audio);
	}

	@Override
	public void stopRecognition(String rid) throws InvalidParameterException{
		var c = contexts.get(rid);
		if(c == null){
			throw new InvalidParameterException("rid", "no valid running request: " + rid);
		}
		c.ws.sendText("{\"eof\" : 1}");
		c.closing = true;
	}
}

package org.langrid.mlgridservices.services.impl;

import org.junit.jupiter.api.Test;
import org.langrid.mlgridservices.service.impl.VoskSpeechToTextService;
import org.langrid.service.ml.ContinuousSpeechRecognitionConfig;
import org.langrid.service.ml.ContinuousSpeechRecognitionReceiverService;
import org.langrid.service.ml.ContinuousSpeechRecognitionTranscript;
import org.springframework.test.context.TestExecutionListeners;

import jp.go.nict.langrid.service_1_2.ProcessFailedException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.ws.client.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import javax.sound.sampled.AudioSystem;

import java.util.ArrayList;

public class VoskSpeechRecognitionTest {
	@Test
	public void test() throws Throwable{
		var base = "./procs/speech_recognition_vosk/";
		var fname =
			"test_ja_2_16k";
			//"test_en_16k";
		var file = base + fname + ".wav";
		try(var w = Files.newBufferedWriter(Path.of(base + fname + "_out.txt"))){
			for (String res : transcribe(file)) {
				System.out.println(res);
				w.write(res);
				w.newLine();
	        }
		}
	}

    private ArrayList<String> results = new ArrayList<String>();
    private CountDownLatch recieveLatch;

    public ArrayList<String> transcribe(String path) throws Exception {
        var factory = new WebSocketFactory();
        var ws = factory.createSocket("ws://localhost:2700");
        ws.addListener(new WebSocketAdapter() {
            @Override
            public void onTextMessage(WebSocket websocket, String message) {
                results.add(message);
                recieveLatch.countDown();
            }
			@Override
			public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
				recieveLatch.countDown();
				cause.printStackTrace();
			}
		});
		ws.connect();

		recieveLatch = new CountDownLatch(1);
		try(var fis = new FileInputStream(new File(path));
			var dis = new DataInputStream(fis)){
			byte[] buf = new byte[8000];
			while (true) {
				int nbytes = dis.read(buf);
				if (nbytes < 0) break;
				ws.sendBinary(buf);
				recieveLatch.await();
			}
			ws.sendText("{\"eof\" : 1}");
			recieveLatch.await();
		} finally{
			ws.disconnect();
		}

		return results;
    }

	@Test
	public void test2() throws Exception {
		var base = "./procs/speech_recognition_vosk/";
		var fname =
			//"test_ja_2_16k";
			"test_en_16k";
		var file = base + fname + ".wav";
		try(var w = Files.newBufferedWriter(Path.of(base + fname + "_out.txt"));
			var ais = AudioSystem.getAudioInputStream(new File(file))){

			var factory = new WebSocketFactory();
	        var ws = factory.createSocket("ws://localhost:2700");
			var count = new AtomicLong();
	        ws.addListener(new WebSocketAdapter() {
    	        @Override
        	    public void onTextMessage(WebSocket websocket, String message) {
					try{
						w.write(message);
						w.newLine();
						System.out.println(count.incrementAndGet());
						System.out.println(message);
					} catch(IOException e){

					}
					//results.add(message);
					recieveLatch.countDown();
				}
				@Override
				public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
					recieveLatch.countDown();
					cause.printStackTrace();
				}
			});
			ws.connect();

			try{
				System.out.println("channels: " + ais.getFormat().getChannels());
				System.out.println("sampleSizeInBits: " + ais.getFormat().getSampleSizeInBits());
				System.out.println("framerate: " + ais.getFormat().getFrameRate());
				byte[] buf = new byte[(int)(
					ais.getFormat().getChannels() *
					ais.getFormat().getSampleSizeInBits() / 8 *
					ais.getFormat().getFrameRate() * 0.2)];
				ws.sendText(String.format("{ \"config\" : { \"sample_rate\" : %d } }", (int)ais.getFormat().getFrameRate()));
				while (true) {
					int nbytes = ais.read(buf);
					if (nbytes < 0) break;
					recieveLatch = new CountDownLatch(1);
					ws.sendBinary(buf);
					recieveLatch.await();
				}
				recieveLatch = new CountDownLatch(1);
				ws.sendText("{\"eof\" : 1}");
				recieveLatch.await();
			} finally{
				ws.disconnect();
			}
		}
	}


	@Test
	public void test3() throws Exception {
		var base = "./procs/speech_recognition_vosk/";
		var fname =
			"test_ja_16k";
			//"test_en_16k";
		var file = base + fname + ".wav";
		var mapper = new ObjectMapper();
		var service = new VoskSpeechToTextService();
		try(var w = Files.newBufferedWriter(Path.of(base + fname + "_out_service.txt"));
			var ais = AudioSystem.getAudioInputStream(new File(file))){
			var af = ais.getFormat();
			var rid = service.startRecognition(
				"ja",
				new ContinuousSpeechRecognitionConfig(af.getChannels(), af.getSampleSizeInBits(), (int)af.getFrameRate()),
				new ContinuousSpeechRecognitionReceiverService() {
					@Override
					public void onRecognitionResult(String recognitionId,
							ContinuousSpeechRecognitionTranscript[] results) {
						try{
							System.out.println("onRecognitionResult");
							var s = mapper.writeValueAsString(results);
							System.out.println(s);
							w.write(s);
							w.newLine();
							System.out.println(s);
						} catch(IOException e){
							e.printStackTrace();
						}
						recieveLatch.countDown();
					}
					@Override
					public void onException(ProcessFailedException exception) {
						recieveLatch.countDown();
						exception.printStackTrace();
					}
				});

			try{
				System.out.println("channels: " + ais.getFormat().getChannels());
				System.out.println("sampleSizeInBits: " + ais.getFormat().getSampleSizeInBits());
				System.out.println("framerate: " + ais.getFormat().getFrameRate());
				byte[] buf = new byte[(int)(
					ais.getFormat().getChannels() *
					ais.getFormat().getSampleSizeInBits() / 8 *
					ais.getFormat().getFrameRate() * 0.2)];
				while (true) {
					int nbytes = ais.read(buf);
					if (nbytes < 0) break;
					recieveLatch = new CountDownLatch(1);
					System.out.println("send " + buf.length + " bytes.");
					service.processRecognition(rid, buf);
					recieveLatch.await();
				}
				recieveLatch = new CountDownLatch(1);
				service.stopRecognition(rid);
				recieveLatch.await();
			} finally{
			}
		}
	}
}

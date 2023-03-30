package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.ValidationUtil;
import org.langrid.service.ml.EmotionRecognitionResult;
import org.langrid.service.ml.SpeechEmotionRecognitionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;

@Service
public class EmpathService implements SpeechEmotionRecognitionService{
	@Value("${services.empath.endpoint}")
	private String endpoint;
	@Value("${services.empath.api-key}")
	private String apiKey;

	private Set<String> supportedAudioFormats = new HashSet<>(Arrays.asList(
			"audio/wav", "audio/x-wav"));

	@Override
	public EmotionRecognitionResult[] recognize(
		byte[] audio, String audioFormat, String audioLanguage)
	throws InvalidParameterException, ProcessFailedException{
		var temp = new File("temp/" + EmpathService.class.getName());
		temp.mkdirs();
		try{
			System.out.println("recignize called. " + audio.length);
			var f = FileUtil.createUniqueFileWithDateTime(temp, "recording-", ".wav");
			Files.write(f.toPath(), audio);
		} catch(IOException e){
			throw new ProcessFailedException(e);
		}

		ValidationUtil.getValidAudioFormat("audioFormat", audioFormat, supportedAudioFormats);
		var builder = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.STRICT)
				.setContentType(ContentType.MULTIPART_FORM_DATA)
				.addTextBody("apikey", apiKey)
				.addBinaryBody("wav", audio);
		var httpPost = new HttpPost(endpoint);
		httpPost.setEntity(builder.build());
		var om = new ObjectMapper();
		try(var t = ServiceInvokerContext.startServiceTimer();
				var client = HttpClients.createDefault();
				var resp = client.execute(httpPost)) {
			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				var node = om.readTree(EntityUtils.toString(resp.getEntity()));
				if(node.get("error").asInt() == 0) {
					var ret = new ArrayList<EmotionRecognitionResult>();
					for(var emo : new String[] {"calm", "anger", "joy", "sorrow", "energy"}) {
						ret.add(new EmotionRecognitionResult(emo, node.get(emo).asDouble() / 50));
					}
					return ret.toArray(new EmotionRecognitionResult[] {});
				} else {
					throw new ProcessFailedException(node.get("msg").asText());
				}
			}
			throw new ProcessFailedException(resp.getStatusLine().toString());
		} catch(IOException e) {
			throw new ProcessFailedException(e);
		}
	}
}

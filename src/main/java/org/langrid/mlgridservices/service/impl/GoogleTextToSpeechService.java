package org.langrid.mlgridservices.service.impl;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.langrid.mlgridservices.util.LanguageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jp.go.nict.langrid.service_1_2.AccessLimitExceededException;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.NoAccessPermissionException;
import jp.go.nict.langrid.service_1_2.NoValidEndpointsException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.ServerBusyException;
import jp.go.nict.langrid.service_1_2.ServiceNotActiveException;
import jp.go.nict.langrid.service_1_2.ServiceNotFoundException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;
import jp.go.nict.langrid.service_1_2.speech.Speech;
import jp.go.nict.langrid.service_1_2.speech.TextToSpeechService;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.TextToSpeechSettings;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;

import java.io.FileInputStream;
import java.io.IOException;

@Service
public class GoogleTextToSpeechService implements TextToSpeechService{
	@Override
	public Speech speak(String language, String text, String voiceType, String audioType)
			throws AccessLimitExceededException, InvalidParameterException, NoAccessPermissionException,
			NoValidEndpointsException, ProcessFailedException, ServerBusyException, ServiceNotActiveException,
			ServiceNotFoundException, UnsupportedLanguageException {
		var lang = language.toLowerCase();
		var vt = voiceType.toLowerCase();
		var c = configs.get(Config.key(lang, vt));
		if(c == null){
			for(var e : configs.entrySet()){
				var l = e.getKey();
				if((LanguageUtil.matches(lang, l) || LanguageUtil.matches(l, lang)) &&
						(vt.isEmpty() || vt.equals(e.getValue().modelName.toLowerCase()))){
					c = e.getValue();
					configs.put(Config.key(lang, vt), c);
					break;
				}
			}
		}
		if(c == null){
			throw new InvalidParameterException("language,voiceType",
				 "no supported combinations of language and voiceType");
		}

		try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(settings)) {
			var input = SynthesisInput.newBuilder().setText(text).build();
			var voice = VoiceSelectionParams.newBuilder()
					.setLanguageCode(c.langCode)
					.setSsmlGender(SsmlVoiceGender.valueOf(c.ssmlGender))
					.setName(c.modelName)
					.build();
			var audioConfig = AudioConfig.newBuilder()
					.setAudioEncoding(AudioEncoding.MP3).build();
			var response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
			var audioContents = response.getAudioContent();
			return new Speech(voiceType, audioType, audioContents.toByteArray());
		} catch(IOException e){
			throw new ProcessFailedException(e);
		}	
	}

	@Override
	public String[] getSupportedLanguages()
			throws AccessLimitExceededException, NoAccessPermissionException, NoValidEndpointsException,
			ProcessFailedException, ServerBusyException, ServiceNotActiveException, ServiceNotFoundException {
		return languages.toArray(new String[]{});
	}

	@Override
	public String[] getSupportedAudioTypes()
			throws AccessLimitExceededException, NoAccessPermissionException, NoValidEndpointsException,
			ProcessFailedException, ServerBusyException, ServiceNotActiveException, ServiceNotFoundException {
		return new String[]{"audio/mpeg"};
	}

	@Override
	public String[] getSupportedVoiceTypes()
			throws AccessLimitExceededException, NoAccessPermissionException, NoValidEndpointsException,
			ProcessFailedException, ServerBusyException, ServiceNotActiveException, ServiceNotFoundException {
		return voices.toArray(new String[]{});
	}

	@PostConstruct
	private void init() {
		try(var is = new FileInputStream(apiKeyFile)){
			var credentialsProvider = FixedCredentialsProvider.create(
				ServiceAccountCredentials.fromStream(is));
			settings = TextToSpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
		} catch(Exception e){
			throw new RuntimeException(e);
		}

		try(var is = GoogleTextToSpeechService.class.getResourceAsStream("GoogleTextToSpeechService_langs.json")){
			var node = new ObjectMapper().readTree(is);
			for(var n : node.get("voices")){
				var modelName = n.get("name").asText();
				var ssmlGender = n.get("ssmlGender").asText();
				voices.add(modelName);
				for(var langCode : n.get("languageCodes")){
					var code = langCode.asText();
					var c = new Config(code, ssmlGender, modelName);
					configs.put(c.key(), c);
					languages.add(code);
				}
			}
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	static class Config{
		private String langCode;
		private String ssmlGender;
		private String modelName;
		public Config(String langCode, String ssmlGender, String modelName) {
			this.langCode = langCode;
			this.ssmlGender = ssmlGender;
			this.modelName = modelName;
		}
		public String key(){
			return langCode.toLowerCase() + "-" + modelName.toLowerCase();
		}
		public static String key(String langCode, String modelName){
			return langCode.toLowerCase() + "-" + modelName.toLowerCase();
		}
	}

	private Map<String, Config> configs = new TreeMap<>();
	private Set<String> voices = new TreeSet<>();
	private Set<String> languages = new TreeSet<>();

	private TextToSpeechSettings settings;
	@Value("${services.google.tts-api-key-file}")
	private String apiKeyFile;
}

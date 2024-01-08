package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.langrid.mlgridservices.service.AbstractPipelineService;
import org.langrid.mlgridservices.service.Instance;
import org.langrid.mlgridservices.service.ProcessInstance;
import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.service.ml.Audio;
import org.langrid.service.ml.TextToSpeechService;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.go.nict.langrid.commons.lang.StringUtil;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PipelineExternalCommandTextToSpeech 
extends AbstractCmdRepl
implements TextToSpeechService{
	public PipelineExternalCommandTextToSpeech(String baseDir){
		super(baseDir);
	}

	public PipelineExternalCommandTextToSpeech(
		String baseDir, String... commands) {
		super(baseDir, commands);
	}

	@Override
	public Audio speak(String text, String textLanguage)
	throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException {
		try{
			var baseFile = FileUtil.createUniqueFileWithDateTime(
				tempDir, "", "");
			var inputTextFile = new File(baseFile.toString() + ".input_text.txt");
			Files.writeString(inputTextFile.toPath(), text, StandardCharsets.UTF_8);
			var inputFile = new File(baseFile.toString() + ".input.txt");
			var outputFile = new File(baseFile.toString() + ".output.wav");
			var input = m.writeValueAsString(new TextToSpeechCommandInput(
				tempDir.getName() + "/" + inputTextFile.getName(),
				textLanguage,
				tempDir.getName() + "/" + outputFile.getName()
			));
			Files.writeString(inputFile.toPath(), input, StandardCharsets.UTF_8);
			var i = getInstance();
			var success = i.exec(input).isSucceeded();
			if(success && outputFile.exists()){
				return new Audio(
					Files.readAllBytes(outputFile.toPath()),
					"audio/x-wav");
			}
			return null;
		} catch(IOException e){
			throw new RuntimeException(e);
		} catch(InterruptedException e){
			return null;
		}
	}

	protected ProcessInstance newProcessInstance(Process process)
	throws IOException{
		return new ProcessInstance(process);
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	static class TextToSpeechCommandInput{
		private String textPath;
		private String textLanguage;
		private String outputPath;
	}

	private ObjectMapper m = new ObjectMapper();;

	private File baseDir;
	private String[] commands;

	private String instanceKey;
	private File tempDir;
}

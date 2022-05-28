package org.langrid.mlgridservices.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.langrid.service.ml.TextSentimentLabel;
import org.langrid.mlgridservices.util.LanguageUtil;
import org.langrid.service.ml.TextSentimentAnalysisResult;
import org.langrid.service.ml.TextSentimentAnalysisService;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class HuggingFaceTextSentimentAnalysisService
implements TextSentimentAnalysisService{
	private File baseDir = new File("./procs/text_sentiment_analysis_huggingface");

	public HuggingFaceTextSentimentAnalysisService(){
	}

	@Override
	public TextSentimentAnalysisResult analyze(String language, String text)
			throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException {
		if(!LanguageUtil.matches("ja", language))
				throw new UnsupportedLanguageException("language", language);
		try {
			var r = run(text)[0];
			return new TextSentimentAnalysisResult(
				r.label.equals("ポジティブ") ? TextSentimentLabel.POSITIVE : TextSentimentLabel.NEGATIVE,
				r.score);
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	static class Result{
		String label;
		double score;
		public void setLabel(String label) {
			this.label = label;
		}
		public void setScore(double score) {
			this.score = score;
		}
	}

	public Result[] run(String text){
		try{
			var cmd = "PATH=$PATH:/usr/local/bin /usr/local/bin/docker-compose " +
					"run --rm huggingface python run.py " +
				" \"" + text.replaceAll("\"", "\\\"") + "\" ";
			System.out.println(cmd);
			var pb = new ProcessBuilder("bash", "-c", cmd);
			pb.directory(baseDir);
			var proc = pb.start();
			try {
				proc.waitFor();
				var res = proc.exitValue();
				if(res == 0) {
					var br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
					String line = null;
					while ((line = br.readLine()) != null) {
						if(!line.startsWith("[{")) continue;
						return mapper.readValue(line, Result[].class);
					}
					throw new RuntimeException("no results found.");
				} else {
					var br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
					var lines = new StringBuilder();
					String line = null;
					while ((line = br.readLine()) != null) {
						lines.append(line);
					}
					throw new RuntimeException(lines.toString());
				}
			} finally {
				proc.destroy();
			}
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private ObjectMapper mapper = new ObjectMapper();
}

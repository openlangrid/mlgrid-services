package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.ProcessUtil;
import org.langrid.service.ml.TextSentimentAnalysisResult;
import org.langrid.service.ml.TextSentimentAnalysisService;
import org.langrid.service.ml.TextSentimentLabel;

import com.fasterxml.jackson.databind.json.JsonMapper;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class ExternalTextSentimentAnalysisService
implements TextSentimentAnalysisService{
	public ExternalTextSentimentAnalysisService(){
		this.baseDir = new File("./procs/text_sentiment_analysis_huggingface");
		this.modelName = "koheiduck/bert-japanese-finetuned-sentiment";
		this.tokenizerName = "cl-tohoku/bert-base-japanese-whole-word-masking";
	}

	public ExternalTextSentimentAnalysisService(String baseDir, String modelName, String tokenizerName) {
		this.baseDir = new File(baseDir);
		this.modelName = modelName;
		this.tokenizerName = tokenizerName;
	}

	public File getBaseDir() {
		return baseDir;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getTokenizerName() {
		return tokenizerName;
	}

	public void setTokenizerName(String tokenizerName) {
		this.tokenizerName = tokenizerName;
	}

	private static class Result{
		public void setLabel(String label) {
			this.label = label;
		}
		public void setScore(double score) {
			this.score = score;
		}
		private String label;
		private double score;
	}
	public TextSentimentAnalysisResult analyze(String text, String textLanguage)
	throws InvalidParameterException, UnsupportedLanguageException, ProcessFailedException {
		try{
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var baseFile = FileUtil.createUniqueFileWithDateTime(
				tempDir, "", "");
			var inputFile = new File(baseFile.toString() + ".input.txt");
			var outputFile = new File(baseFile.toString() + ".output.txt");
			Files.writeString(inputFile.toPath(), text, StandardCharsets.UTF_8);
			run("temp", inputFile.getName(), textLanguage, outputFile.getName());
			if(!outputFile.exists()) return null;
			var ret = new JsonMapper().readValue(
				Files.newBufferedReader(outputFile.toPath(), StandardCharsets.UTF_8)
				, Result.class);
			return new TextSentimentAnalysisResult(toLabel(ret.label), ret.score);
		} catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	private static TextSentimentLabel toLabel(String name){
		switch(name.toLowerCase()){
			case "positive":
			case "ポジティブ":
				return TextSentimentLabel.POSITIVE;
			case "neutral":
			case "ニュートラル":
				return TextSentimentLabel.NEUTRAL;
			case "negative":
			case "ネガティブ":
				return TextSentimentLabel.NEGATIVE;
		}
		return null;
	}

	public void run(String dirName, String inputFileName, String inputLanguage, String outputFileName){
		try(var l = ServiceInvokerContext.acquireGpuLock()){
			var cmd = String.format(
				"PATH=$PATH:/usr/local/bin /usr/local/bin/docker-compose run --rm " +
				"service python analyze.py --model %1$s " +
				"--tokenizer %2$s " +
				"--inputPath ./%3$s/%4$s " +
				"--inputLanguage %5$s " + 
				"--outputPath ./%3$s/%6$s",
				modelName, tokenizerName, dirName,
				inputFileName, inputLanguage,
				outputFileName);
			ServiceInvokerContext.exec(()->{
				ProcessUtil.runAndWaitWithInheritingOutput(cmd, baseDir);
			}, "execution", "docker-compose");
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	private File baseDir;
	private String modelName;
	private String tokenizerName;
}

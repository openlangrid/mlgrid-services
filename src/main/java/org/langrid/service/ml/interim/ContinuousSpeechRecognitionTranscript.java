package org.langrid.service.ml.interim;

public class ContinuousSpeechRecognitionTranscript {
	public ContinuousSpeechRecognitionTranscript() {
	}

	public ContinuousSpeechRecognitionTranscript(
		int sentenceId, int startMillis, int endMillis, String sentence,
			boolean fixed, double accuracy) {
		super();
		this.sentenceId = sentenceId;
		this.startMillis = startMillis;
		this.endMillis = endMillis;
		this.sentence = sentence;
		this.fixed = fixed;
		this.accuracy = accuracy;
	}

	public int getSentenceId() {
		return sentenceId;
	}
	public void setSentenceId(int sentenceId) {
		this.sentenceId = sentenceId;
	}
	public int getStartMillis() {
		return startMillis;
	}
	public void setStartMillis(int startMillis) {
		this.startMillis = startMillis;
	}
	public int getEndMillis() {
		return endMillis;
	}
	public void setEndMillis(int endMillis) {
		this.endMillis = endMillis;
	}
	public String getSentence() {
		return sentence;
	}
	public void setSentence(String sentence) {
		this.sentence = sentence;
	}
	public boolean isFixed() {
		return fixed;
	}
	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}
	public double getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}

	private int sentenceId;
	private int startMillis;
	private int endMillis;
	private String sentence;
	private boolean fixed;
	private double accuracy;
}

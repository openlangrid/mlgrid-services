package org.langrid.service.ml;

public class ContinuousSpeechRecognitionTranscript {
	public ContinuousSpeechRecognitionTranscript() {
	}

	public ContinuousSpeechRecognitionTranscript(int sentenceId, String sentence, boolean fixed, double accuracy) {
		super();
		this.sentenceId = sentenceId;
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
	private String sentence;
	private boolean fixed;
	private double accuracy;
}

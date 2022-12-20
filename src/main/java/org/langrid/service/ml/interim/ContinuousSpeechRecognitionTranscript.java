package org.langrid.service.ml.interim;

public class ContinuousSpeechRecognitionTranscript {
	public ContinuousSpeechRecognitionTranscript() {
	}

	public ContinuousSpeechRecognitionTranscript(
		int sentenceId, int start, int end, String sentence, boolean fixed, double accuracy) {
		super();
		this.sentenceId = sentenceId;
		this.start = start;
		this.end = end;
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
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
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
	private int start;
	private int end;
	private String sentence;
	private boolean fixed;
	private double accuracy;
}

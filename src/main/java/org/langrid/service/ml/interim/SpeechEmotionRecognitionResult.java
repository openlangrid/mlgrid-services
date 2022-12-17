package org.langrid.service.ml.interim;

public class SpeechEmotionRecognitionResult {
	public SpeechEmotionRecognitionResult() {
	}
	public SpeechEmotionRecognitionResult(String label, double degree) {
		this.label = label;
		this.degree = degree;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public double getDegree() {
		return degree;
	}
	public void setDegree(double degree) {
		this.degree = degree;
	}
	
	private String label;
	private double degree;
}

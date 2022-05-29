package org.langrid.service.ml;

public class SpeechEmotionRecognitionResult {
	private String label;
	private double degree; // 0.0-1.0

	public SpeechEmotionRecognitionResult() {
	}
	public SpeechEmotionRecognitionResult(String label, double degree) {
		super();
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
}

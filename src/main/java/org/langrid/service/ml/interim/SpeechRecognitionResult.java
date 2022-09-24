package org.langrid.service.ml.interim;

public class SpeechRecognitionResult {
	public SpeechRecognitionResult(){
	}
	public SpeechRecognitionResult(int startMillis, int endMillis, String transcript) {
		this.startMillis = startMillis;
		this.endMillis = endMillis;
		this.transcript = transcript;
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
	public String getTranscript() {
		return transcript;
	}
	public void setTranscript(String transcript) {
		this.transcript = transcript;
	}

	private int startMillis;
	private int endMillis;
	private String transcript;
}

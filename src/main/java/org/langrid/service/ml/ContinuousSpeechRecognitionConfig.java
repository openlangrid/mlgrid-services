package org.langrid.service.ml;

public class ContinuousSpeechRecognitionConfig {
	private int channels;
	private int sampleSizeInBits;
	private int frameRate;  // hertz. 8000, 16000, 44100.

	public ContinuousSpeechRecognitionConfig(){
	}

	public ContinuousSpeechRecognitionConfig(int channels, int sampleSizeInBits, int frameRate) {
		this.channels = channels;
		this.sampleSizeInBits = sampleSizeInBits;
		this.frameRate = frameRate;
	}

	public int getChannels() {
		return channels;
	}
	public void setChannels(int channels) {
		this.channels = channels;
	}
	public int getSampleSizeInBits() {
		return sampleSizeInBits;
	}
	public void setSampleSizeInBits(int sampleSizeInBits) {
		this.sampleSizeInBits = sampleSizeInBits;
	}
	public int getFrameRate() {
		return frameRate;
	}
	public void setFrameRate(int frameRate) {
		this.frameRate = frameRate;
	}
	
}

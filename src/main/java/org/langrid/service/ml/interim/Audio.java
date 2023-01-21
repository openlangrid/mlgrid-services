package org.langrid.service.ml.interim;

public class Audio {
	public Audio(){
	}

	public Audio(byte[] audio, String format) {
		this.audio = audio;
		this.format = format;
	}

	public byte[] getAudio() {
		return audio;
	}
	public void setAudio(byte[] audio) {
		this.audio = audio;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}

	private byte[] audio;
	private String format;
}

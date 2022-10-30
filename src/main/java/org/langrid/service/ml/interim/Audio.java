package org.langrid.service.ml.interim;

public class Audio {
	public Audio(){
	}

	public Audio(String format, byte[] audio) {
		this.format = format;
		this.audio = audio;
	}

	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public byte[] getAudio() {
		return audio;
	}
	public void setAudio(byte[] audio) {
		this.audio = audio;
	}

	private String format;
	private byte[] audio;
}

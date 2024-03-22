package org.langrid.service.ml.interim;

public class Video {
	public Video(){}
	
	public Video(byte[] video, String format) {
		this.video = video;
		this.format = format;
	}

	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public byte[] getVideo() {
		return video;
	}
	public void setVideo(byte[] video) {
		this.video = video;
	}

	private byte[] video;
	private String format;
}

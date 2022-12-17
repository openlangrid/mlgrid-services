package org.langrid.service.ml.interim;

public class Image {
	public Image(){}
	
	public Image(byte[] image, String format) {
		this.image = image;
		this.format = format;
	}

	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public byte[] getImage() {
		return image;
	}
	public void setImage(byte[] image) {
		this.image = image;
	}

	private byte[] image;
	private String format;
}

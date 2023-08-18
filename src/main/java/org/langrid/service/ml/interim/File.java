package org.langrid.service.ml.interim;

public class File {
	private byte[] content;
	private String format;  // mime type

	public File() {
	}

	public File(byte[] content, String format) {
		this.content = content;
		this.format = format;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
}

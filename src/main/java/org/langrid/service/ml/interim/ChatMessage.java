package org.langrid.service.ml.interim;

public class ChatMessage {
	public ChatMessage(){
	}
	public ChatMessage(String role, String content, String contentLanguage) {
		this.role = role;
		this.content = content;
		this.contentLanguage = contentLanguage;
	}

	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getContentLanguage() {
		return contentLanguage;
	}
	public void setContentLanguage(String contentLanguage) {
		this.contentLanguage = contentLanguage;
	}

	private String role;
	private String content;
	private String contentLanguage;
}

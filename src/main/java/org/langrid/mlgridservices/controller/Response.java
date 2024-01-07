package org.langrid.mlgridservices.controller;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {
	private Map<String, Object> headers = Collections.emptyMap();
	private Object result;
	private Error error;
	public Response(Object result) {
		this.result = result;
	}
	public Response(Error error) {
		this.error = error;
	}
	public void putHeader(String name, Object value){
		headers().put(name, value);
	}

	public void putAllHeaders(Map<String, Object> headers){
		headers().putAll(headers);
	}

	private Map<String, Object> headers(){
		if(headers == null || headers.size() == 0){
			headers = new TreeMap<>();
		}
		return headers;
	}
}

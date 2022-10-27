package org.langrid.mlgridservices.controller;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {
	private Map<String, String> headers;
	private Object result;
	private Error error;
	public Response(Object result) {
		this.result = result;
	}
	public Response(Error error) {
		this.error = error;
	}
}

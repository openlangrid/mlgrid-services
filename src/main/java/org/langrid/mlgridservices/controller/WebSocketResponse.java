package org.langrid.mlgridservices.controller;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class WebSocketResponse extends Response{
	private int reqId;

	public WebSocketResponse(int reqId, Map<String, Object> headers, Object result) {
		this.reqId = reqId;
		this.setHeaders(headers);
		this.setResult(result);
	}

	public WebSocketResponse(int reqId, Error error) {
		this.reqId = reqId;
		this.setError(error);
	}

	public WebSocketResponse(int reqId, Map<String, Object> headers, Error error) {
		this.reqId = reqId;
		this.setHeaders(headers);
		this.setError(error);
	}
}

package org.langrid.mlgridservices.controller;

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
	public WebSocketResponse(int reqId, Object result) {
		this.reqId = reqId;
		this.setResult(result);
	}
	public WebSocketResponse(int reqId, Error error) {
		this.reqId = reqId;
		this.setError(error);
	}
}

package org.langrid.mlgridservices.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {
	private Object result;
	private Error error;
	public Response(Object result) {
		this.result = result;
	}
	public Response(Error error) {
		this.error = error;
	}
}

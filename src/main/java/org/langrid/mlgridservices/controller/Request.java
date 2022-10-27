package org.langrid.mlgridservices.controller;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class Request {
	private Map<String, String> headers;
	private String method;
	private Object[] args;
}

package org.langrid.mlgridservices.controller;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class Request {
	private String method;
	private Object[] args;
}

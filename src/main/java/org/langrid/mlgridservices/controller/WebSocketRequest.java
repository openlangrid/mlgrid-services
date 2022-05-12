package org.langrid.mlgridservices.controller;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WebSocketRequest extends Request{
	private int reqId;
	private String serviceId;
}

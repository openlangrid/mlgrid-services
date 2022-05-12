package org.langrid.mlgridservices.service;

import org.langrid.mlgridservices.controller.Request;
import org.langrid.mlgridservices.controller.Response;

public interface Service {
	Response invoke(String serviceId, Request invocation);
}

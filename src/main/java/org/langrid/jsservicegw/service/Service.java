package org.langrid.jsservicegw.service;

import org.langrid.jsservicegw.controller.Request;
import org.langrid.jsservicegw.controller.Response;

public interface Service {
	Response invoke(String serviceId, Request invocation);
}

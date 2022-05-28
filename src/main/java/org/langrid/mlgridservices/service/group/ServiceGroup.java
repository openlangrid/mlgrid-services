package org.langrid.mlgridservices.service.group;

import org.langrid.mlgridservices.controller.Request;
import org.langrid.mlgridservices.controller.Response;

public interface ServiceGroup {
	Response invoke(String serviceId, Request invocation);
}

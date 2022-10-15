package org.langrid.mlgridservices.service.group;

import java.util.List;

import org.langrid.mlgridservices.controller.Request;
import org.langrid.mlgridservices.controller.Response;

import jp.go.nict.langrid.commons.util.Pair;

public interface ServiceGroup {
	List<Pair<String, Class<?>>> listServices();
	Response invoke(String serviceId, Request invocation);
}

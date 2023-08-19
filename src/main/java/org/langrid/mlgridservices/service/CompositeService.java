package org.langrid.mlgridservices.service;

import org.langrid.service.ml.interim.management.ServiceInvocation;

public interface CompositeService {
	ServiceInvocation[] getInvocations();
}

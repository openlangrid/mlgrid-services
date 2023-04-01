package org.langrid.mlgridservices.service.impl.test;

import org.langrid.service.ml.interim.TestService;

import jp.go.nict.langrid.service_1_2.ProcessFailedException;

public class ProcessFailedExceptionService
implements TestService{
	@Override
	public Object test(Object arg) throws ProcessFailedException {
		throw new ProcessFailedException("Exception!!");
	}
}

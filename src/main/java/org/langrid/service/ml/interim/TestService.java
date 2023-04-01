package org.langrid.service.ml.interim;

import jp.go.nict.langrid.service_1_2.ProcessFailedException;

public interface TestService {
	Object test(Object arg) throws ProcessFailedException;
}

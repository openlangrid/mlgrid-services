package org.langrid.service.ml.interim.management;

public interface ServiceManagementService {
	SearchServicesResult searchServices(
		int startIndex, int maxCount, MatchingCondition[] conditions, Order[] orders);
}

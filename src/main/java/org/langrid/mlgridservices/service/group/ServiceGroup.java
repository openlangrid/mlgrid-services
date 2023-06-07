package org.langrid.mlgridservices.service.group;

import java.util.List;

import jp.go.nict.langrid.commons.util.Pair;

public interface ServiceGroup {
	List<Pair<String, Class<?>>> listServices();
	<T> T get(String serviceId);
}

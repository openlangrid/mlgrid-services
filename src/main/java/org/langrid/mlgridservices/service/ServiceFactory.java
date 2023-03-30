package org.langrid.mlgridservices.service;

public interface ServiceFactory {
	<T> T create(String name, Class<T> intf);
}

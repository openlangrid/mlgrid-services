package org.langrid.mlgridservices.service;

public interface ServiceFactory {
	@SuppressWarnings("unchecked")
	default <T> T create(String name, Class<T> intf) {
		return (T)create(name);
	}
	Object create(String name);
}

package org.langrid.service.ml.interim.management;

public class ServiceEntry {
	public ServiceEntry(){}

	public ServiceEntry(String serviceId, String serviceType) {
		this.serviceId = serviceId;
		this.serviceType = serviceType;
	}

	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	public String getServiceType() {
		return serviceType;
	}
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	private String serviceId;
	private String serviceType;
}

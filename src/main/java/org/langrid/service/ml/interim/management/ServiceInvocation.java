package org.langrid.service.ml.interim.management;

public class ServiceInvocation {
	public ServiceInvocation(){}

	public ServiceInvocation(String invocationName, String serviceType) {
		this.invocationName = invocationName;
		this.serviceType = serviceType;
	}

	public String getInvocationName() {
		return invocationName;
	}

	public void setInvocationName(String invocationName) {
		this.invocationName = invocationName;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	private String invocationName;
	private String serviceType;
}

package org.langrid.service.ml.interim.management;

public class ServiceEntry {
	public ServiceEntry(){}

	public ServiceEntry(String serviceId, String serviceType) {
		this.serviceId = serviceId;
		this.serviceType = serviceType;
	}

	public ServiceEntry(String serviceId, String serviceType, String description, String license, String url) {
		this.serviceId = serviceId;
		this.serviceType = serviceType;
		this.description = description;
		this.license = license;
		this.url = url;
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
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getLicense() {
		return license;
	}
	public void setLicense(String license) {
		this.license = license;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	private String serviceId;
	private String serviceType;
	private String description;
	private String license;
	private String url;
}

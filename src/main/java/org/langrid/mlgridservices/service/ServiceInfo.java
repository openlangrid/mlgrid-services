package org.langrid.mlgridservices.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceInfo {
	private String serviceId;
	private String description;
	private String license;
	private String url;
}

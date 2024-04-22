package org.langrid.mlgridservices;

import org.langrid.mlgridservices.util.GpuSpec;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix="mlgrid")
@Data
public class ApplicationYaml {
	private GpuSpec[] availableGpus;
	private String[] apiKeys;
	private String[] allowedOrigines;
}

package org.langrid.mlgridservices.controller;

import org.springframework.web.bind.annotation.RestController;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.service.InstancePool.Gpu;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
public class GpuController {
	@GetMapping("/gpus")
	public Gpu[] list() {
		return ServiceInvokerContext.getInstancePool().getGpus();
	}		
}

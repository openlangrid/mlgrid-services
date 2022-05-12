package org.langrid.jsservicegw.controller;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

import org.langrid.jsservicegw.service.ServiceInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/services")
public class ServiceController {
	@PostMapping("/{serviceId}")
	public synchronized @ResponseBody Response invoke(
			@PathVariable("serviceId") String serviceId,
			@RequestBody Request request) throws MalformedURLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		System.out.println(invoker);
		return invoker.invoke(serviceId, request);
	}

	@Autowired
	private ServiceInvoker invoker;
}

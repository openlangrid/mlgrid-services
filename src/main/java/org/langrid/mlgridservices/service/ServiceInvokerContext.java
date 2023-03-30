package org.langrid.mlgridservices.service;

import java.util.function.BiFunction;

import org.langrid.mlgridservices.util.GPULock;
import org.langrid.mlgridservices.util.Timer;

public class ServiceInvokerContext implements AutoCloseable{
	public static ServiceInvokerContext get(){
		return ctx.get();
	}

	public static <T> ServiceInvokerContext create(ServiceFactory factory){
		var c = new ServiceInvokerContext(factory);
		ctx.set(c);
		return c;
	}

	public static GPULock acquireGpuLock() throws InterruptedException{
		return GPULock.acquire();
	}

	public static Timer startServiceTimer(){
		return get().timer().startChild("Service");
	}

	private ServiceInvokerContext(ServiceFactory factory){
		this.factory = factory;
		this.timer = new Timer("ServiceInvoker");
	}

	@Override
	public void close() {
		timer.close();
	}

	public Timer timer(){
		return timer;
	}

	public <T> T resolveService(String bindingName, Class<T> intf){
		return factory.create(bindingName, intf);
	}

	public <T> T getService(String serviceName, Class<T> intf){
		return factory.create(serviceName, intf);
	}

	private static ThreadLocal<ServiceInvokerContext> ctx = new ThreadLocal<>(){
		protected ServiceInvokerContext initialValue() {
			return null;
		};
	};

	private ServiceFactory factory;
	private Timer timer;
}

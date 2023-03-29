package org.langrid.mlgridservices.service;

import java.util.function.BiFunction;

import org.langrid.mlgridservices.util.GPULock;
import org.langrid.mlgridservices.util.Timer;

public class ServiceInvokerContext implements AutoCloseable{
	public static ServiceInvokerContext get(){
		return ctx.get();
	}

	public static <T> ServiceInvokerContext create(BiFunction<String, Class<T>, T> factory){
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

	private <T> ServiceInvokerContext(BiFunction<String, Class<T>, T> factory){
		this.factory = (BiFunction)factory;
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
		return (T)factory.apply(bindingName, intf);
	}

	public <T> T getService(String serviceName, Class<T> intf){
		return (T)factory.apply(serviceName, intf);
	}

	private static ThreadLocal<ServiceInvokerContext> ctx = new ThreadLocal<>(){
		protected ServiceInvokerContext initialValue() {
			return null;
		};
	};

	private BiFunction<String, Class<?>, ?> factory;
	private Timer timer;
}

package org.langrid.mlgridservices.service;

import org.langrid.mlgridservices.util.Timer;

public class ServiceInvokerContext implements AutoCloseable{
	public static ServiceInvokerContext get(){
		return ctx.get();
	}

	public static Timer startServiceTimer(){
		return get().timer().startChild("Service");
	}

	public static ServiceInvokerContext create(){
		var c = new ServiceInvokerContext();
		ctx.set(c);
		return c;
	}

	private ServiceInvokerContext(){
		timer = new Timer("ServiceInvoker");
	}

	@Override
	public void close() {
		timer.close();
	}

	public Timer timer(){
		return timer;
	}

	private static ThreadLocal<ServiceInvokerContext> ctx = new ThreadLocal<>(){
		protected ServiceInvokerContext initialValue() {
			return new ServiceInvokerContext();
		};
	};
	private Timer timer;
}

package org.langrid.mlgridservices.service;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.langrid.mlgridservices.util.GPULock;
import org.langrid.mlgridservices.util.Timer;
import org.springframework.cglib.proxy.Proxy;

public class ServiceInvokerContext implements AutoCloseable{
	public static ServiceInvokerContext get(){
		return ctx.get().getLast();
	}

	public static <T> ServiceInvokerContext start(
		ServiceFactory factory, Map<String, Object> headers){
		var c = new ServiceInvokerContext(factory, headers);
		ctx.get().addLast(c);
		return c;
	}

	public static <T> ServiceInvokerContext start(String invocationName){
		var l = ctx.get();
		var last = l.getLast();
		// bindingsだけネストする
		var h = createChildHeaders(last.getHeaders(), invocationName);
		return start(last.getFactory(), h);
	}

	@SuppressWarnings("unchecked")
	static Map<String, Object> createChildHeaders(Map<String, Object> headers, String invocationName){
		var ret = new HashMap<>(headers);
		do{
			var b = (Map<String, Object>)headers.get("bindings");
			if(b == null || b.size() == 0) break;
			var ci = b.get(invocationName);
			if(ci == null || !(ci instanceof Map)) break;
			var ccb = (Map<String, Object>)((Map<String, Object>)ci).get("bindings");
			if(ccb == null){
				ccb = new HashMap<>();
			}
			ret.put("bindings", ccb);
		} while(false);
		return ret;
	}

	public static void finish(){
		ctx.get().removeLast();
	}

	public static GPULock acquireGpuLock() throws InterruptedException{
		return GPULock.acquire();
	}

	public static Timer startServiceTimer(){
		return get().timer().startChild("Service");
	}

	private ServiceInvokerContext(ServiceFactory factory, Map<String, Object> headers){
		this.factory = factory;
		this.headers = headers;
		this.timer = new Timer("ServiceInvoker");
	}

	@Override
	public void close() {
		timer.close();
	}

	public ServiceFactory getFactory() {
		return factory;
	}

	public Map<String, Object> getHeaders() {
		return headers;
	}

	public Timer timer(){
		return timer;
	}

	@SuppressWarnings("unchecked")
	public <T> T getBindedService(String invocationName, Class<T> intf){
		var bindings = (Map<String, Object>)headers.get("bindings");
		var binding = bindings.get(invocationName);
		String serviceName = null;
		if(binding instanceof String){
			serviceName = (String)binding;
		} else if(binding instanceof Map){
			serviceName = (String)((Map<String, Object>)binding).get("serviceId");
		}
		var s = getService(serviceName, intf);
		return (T)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
			new Class[]{intf}, (Object proxy, Method method, Object[] args) -> {
					ServiceInvokerContext.start(invocationName);
					try{
						return method.invoke(s, args);
					} finally{
						ServiceInvokerContext.finish();
					}
				}
			);
	}

	public <T> T getService(String serviceName, Class<T> intf){
		return factory.create(serviceName, intf);
	}

	private static ThreadLocal<LinkedList<ServiceInvokerContext>> ctx = new ThreadLocal<>(){
		protected LinkedList<ServiceInvokerContext> initialValue() {
			return new LinkedList<>();
		};
	};
	private ServiceFactory factory;
	private Map<String, Object> headers;
	private Timer timer;
}

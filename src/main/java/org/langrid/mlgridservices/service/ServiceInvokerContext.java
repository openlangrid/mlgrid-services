package org.langrid.mlgridservices.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.langrid.mlgridservices.util.GPULock;
import org.langrid.mlgridservices.util.Timer;
import org.springframework.cglib.proxy.Proxy;

import jp.go.nict.langrid.commons.util.function.Functions.RunnableWithException;
import jp.go.nict.langrid.commons.util.function.Functions.SupplierWithException;

public class ServiceInvokerContext {
	public static ServiceInvokerContext initialize(
		ServiceFactory factory, Map<String, Object> requestHeaders,
		String type, String target, Object... args){
		ctxList.remove();
		var rootSpan = new Span(type, target, args);
		@SuppressWarnings("unchecked")
		var bindings = (Map<String, Object>)requestHeaders.get("bindings");
		var root = new ServiceInvokerContext(factory, rootSpan, requestHeaders, bindings);
		ctxList.get().addLast(root);
		return root;
	}

	public static ServiceInvokerContext start(
		String type, String target, Object... args){
		var current = current();
		var span = current.getSpan().newChild(type, target, args);
		var ctx = new ServiceInvokerContext(current.factory, span, current.requestHeaders, current.bindings);
		ctxList.get().addLast(ctx);
		return ctx;
	}

	public static ServiceInvokerContext startInvocation(
		Map<String, Object> children,
		String invocationName, String method, Object... args){
		System.out.println("startInvocation");
		var current = current();
		// bindingsだけネストする
		var requestHeaders = new HashMap<>(current.requestHeaders);
		requestHeaders.put("bindings", children);
		// spanを追加する
		var span = current.getSpan().newChild("invocation", invocationName + "." + method, args);
		var ctx = new ServiceInvokerContext(current.factory, span, requestHeaders, children);
		ctxList.get().addLast(ctx);
		return ctx;
	}

	public static Span finalizeWithResult(Object result){
		var current = current();
		finishWithResult(result);
		return current.span;
	}

	public static Span finalizeWithException(Throwable exception){
		var current = current();
		current.getSpan().finishWithException(exception);
		ctxList.remove();
		return current.span;
	}

	public static void finishWithResult(Object result){
		var current = current();
		current.getSpan().finishWithResult(result);
		ctxList.get().removeLast();
	}

	public static void finishWithException(Throwable exception){
		var current = current();
		current.getSpan().finishWithException(exception);
		ctxList.get().removeLast();
	}

	public static <E extends Exception> void exec(
		RunnableWithException<E> p, String type, String target, Object... args){
		start(type, target, args);
		try{
			p.run();
			finishWithResult((Object)null); 
		} catch(Exception e){
			finishWithException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <R, E extends Exception> R exec(
		SupplierWithException<R, E> p, String type, String target, Object... args)
	throws E{
		start(type, target, args);
		try{
			var ret = p.get();
			finishWithResult(null); 
			return ret;
		} catch(RuntimeException | Error e){
			finishWithException(e);
			throw e;
		} catch(Exception e){
			finishWithException(e);
			throw (E)e;
		}
	}

	public static ServiceInvokerContext current(){
		return ctxList.get().getLast();
	}

	public static GPULock acquireGpuLock() throws InterruptedException{
		return GPULock.acquire();
	}

	private ServiceInvokerContext(
		ServiceFactory factory, Span span,
		Map<String, Object> requestHeaders, Map<String, Object> bindings){
		this.factory = factory;
		this.span = span;
		this.timer = new Timer("ServiceInvoker");
		this.requestHeaders = requestHeaders;
		this.bindings = bindings;
	}

	public ServiceFactory getFactory() {
		return factory;
	}

	public Span getSpan() {
		return span;
	}

	public Timer timer(){
		return timer;
	}

	@SuppressWarnings("unchecked")
	public <T> T getBindedService(String invocationName, Class<T> intf){
		var binding = bindings.get(invocationName);
		String serviceName = null;
		Map<String, Object> children = null;
		if(binding instanceof String){
			serviceName = (String)binding;
		} else if(binding instanceof Map){
			var b = (Map<String, Object>)binding;
			serviceName = (String)b.get("serviceId");
			children = (Map<String, Object>)b.get("children");
		}
		var c = children;
		var s = getService(serviceName, intf);
		return (T)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
			new Class[]{intf}, (Object proxy, Method method, Object[] args) -> {
					ServiceInvokerContext.startInvocation(
						c, invocationName, method.getName(), args);
					try{
						var result = method.invoke(s, args);
						ServiceInvokerContext.finishWithResult(result);
						return result;
					} catch(InvocationTargetException e){
						ServiceInvokerContext.finishWithException(e.getTargetException());
						throw e;
					}
				}
			);
	}

	public <T> T getService(String serviceName, Class<T> intf){
		return factory.create(serviceName, intf);
	}

	private static ThreadLocal<LinkedList<ServiceInvokerContext>> ctxList = new ThreadLocal<>(){
		protected LinkedList<ServiceInvokerContext> initialValue() {
			return new LinkedList<>();
		};
	};

	private ServiceFactory factory;
	private Span span;
	private Timer timer;
	private Map<String, Object> requestHeaders;
	private Map<String, Object> bindings;
}

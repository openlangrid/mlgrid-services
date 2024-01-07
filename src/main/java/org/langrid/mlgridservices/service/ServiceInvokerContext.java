package org.langrid.mlgridservices.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;

import org.langrid.mlgridservices.util.GPULock;
import org.langrid.mlgridservices.util.GpuPool;
import org.langrid.mlgridservices.util.Timer;
import org.springframework.cglib.proxy.Proxy;

import jp.go.nict.langrid.commons.util.function.Functions.RunnableWithException;
import jp.go.nict.langrid.commons.util.function.Functions.SupplierWithException;

public class ServiceInvokerContext {
	@SuppressWarnings("unchecked")
	public static ServiceInvokerContext initialize(
		ServiceFactory factory, Map<String, Object> requestHeaders,
		String type, String target, Object... args){
		ctxList.remove();
		var rootSpan = new Span(type, target, args);
		if(requestHeaders == null){
			requestHeaders = new HashMap<>();
		}
		var bindings = (Map<String, Object>)requestHeaders.getOrDefault(
			"bindings", new HashMap<>());
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
		var newCurrent = current();
		if(newCurrent != null)
			newCurrent.getResponseHeaders().putAll(current.getResponseHeaders());
	}

	public static void finishWithException(Throwable exception){
		var current = current();
		current.getSpan().finishWithException(exception);
		ctxList.get().removeLast();
		var newCurrent = current();
		if(newCurrent != null)
			newCurrent.getResponseHeaders().putAll(current.getResponseHeaders());
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
		if(ctxList.get().size() == 0) return null;
		return ctxList.get().getLast();
	}

	public static synchronized Instance getInstance(String key, Supplier<Instance> supplier)
	throws InterruptedException {
		return instances.computeIfAbsent(key, k->supplier.get());
	}

	public static synchronized Instance getInstanceWithGpuLock(String key, Supplier<Instance> supplier)
	throws InterruptedException {
		if(gpuInstanceKey != null){
			if(!key.equals(gpuInstanceKey)){
				terminateGpuInstance();
			} else{
				return gpuInstance;
			}
		}
		gpuInstanceLock = acquireGpuLock();
		gpuInstance = supplier.get();
		gpuInstanceKey = key;
		gpuInstanceLockedAtMs = System.currentTimeMillis();

		return gpuInstance;
	}

	/**
	 * 今はadditionalGpuIdsに設定されているidから1つだけ割り当てている。gpuCountパラメータは無視。
	 * @param key
	 * @param gpuCount
	 * @param supplier
	 * @return
	 * @throws InterruptedException
	 */
	public static synchronized Instance getInstanceWithPooledGpu(
		String key, int gpuCount, Function<int[], Instance> supplier)
	throws InterruptedException {
		if(gpuPool.getGpuCount() == 0){
			return getInstanceWithGpuLock(key, ()->supplier.apply(new int[]{}));
		}
		var instance = keyToInstance.get(key);
		if(instance != null) return instance;

		if(gpuPool.getAvailableGpuCount() == 0){
			var it = keyToInstance.entrySet().iterator();
			var entry = it.next();
			entry.getValue().terminateAndWait();
			var id = entry.getKey();
			keyToGpu.remove(id).release();
			keyToInstanceStartedAt.remove(id);
			it.remove();
		}

		var gpu = gpuPool.acquire();
		instance = supplier.apply(new int[]{gpu.getId()});
		keyToInstance.put(key, instance);
		keyToInstanceStartedAt.put(key, System.currentTimeMillis());
		keyToGpu.put(key, gpu);

		return instance;
	}

	public GpuPool.Gpu acquireGpu() throws InterruptedException{
		return gpuPool.acquire();
	}

	public static long getGpuInstanceLockedAtMs() {
		return gpuInstanceLockedAtMs;
	}

	public static synchronized void terminateGpuInstanceOlderMsThan(
		long threshold)
	throws InterruptedException {
		if(gpuInstanceLockedAtMs <= 0) return;
		if((System.currentTimeMillis() - gpuInstanceLockedAtMs) >= threshold){
			terminateGpuInstance();
		}
		var it = keyToInstance.entrySet().iterator();
		while(it.hasNext()){
			var entry = it.next();
			var id = entry.getKey();
			var startedAt = keyToInstanceStartedAt.get(id);
			if((System.currentTimeMillis() - startedAt) >= threshold){
				entry.getValue().terminateAndWait();
				keyToGpu.remove(id).release();
				keyToInstanceStartedAt.remove(id);
				it.remove();
			}
		}
	}

	private static synchronized void terminateGpuInstance() throws InterruptedException{
		gpuInstance.terminateAndWait();
		gpuInstance = null;
		gpuInstanceKey = null;
		gpuInstanceLock.release();
		gpuInstanceLock = null;
		gpuInstanceLockedAtMs = -1;
	}

	private static String gpuInstanceKey;
	private static Instance gpuInstance;
	private static long gpuInstanceLockedAtMs;
	private static GPULock gpuInstanceLock;
	private static Map<String, Instance> instances = new HashMap<>();

	public static synchronized GPULock acquireGpuLock() throws InterruptedException{
		if(gpuInstanceLock != null && gpuInstance != null){
			System.out.println("terminate other instance.");
			terminateGpuInstance();
		}
		start("execution", "GPULock.acquire");
		try{
			return GPULock.acquire();
		} finally{
			finishWithResult(null);
		}
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

	public Map<String, Object> getResponseHeaders() {
		return responseHeaders;
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

	public static void setGpuPool(GpuPool pool){
		gpuPool = pool;
	}

	private static GpuPool gpuPool = new GpuPool(0);
	private ServiceFactory factory;
	private Span span;
	private Timer timer;
	private Map<String, Object> requestHeaders;
	private Map<String, Object> bindings;
	private Map<String, Object> responseHeaders = new TreeMap<>();

	private static Map<String, GpuPool.Gpu> keyToGpu = new HashMap<>();
	private static Map<String, Instance> keyToInstance = new LinkedHashMap<>();
	private static Map<String, Long> keyToInstanceStartedAt = new HashMap<>();
}

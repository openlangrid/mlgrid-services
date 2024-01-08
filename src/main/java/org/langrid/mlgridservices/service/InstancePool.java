package org.langrid.mlgridservices.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

import org.langrid.mlgridservices.util.GpuSpec;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jp.go.nict.langrid.commons.util.ArrayUtil;
import jp.go.nict.langrid.commons.util.function.Functions;
import jp.go.nict.langrid.commons.util.function.SoftenedException;
import lombok.Data;
import lombok.NoArgsConstructor;

public class InstancePool {
	static record InstanceEntry(String instanceId, @JsonIgnore Instance instance, long startedAt, GpuSpec[] gpus){
		private static final GpuSpec[] empty = {};
		public InstanceEntry(String instanceId, Instance instance){
			this(instanceId, instance, System.currentTimeMillis(), empty);
		}
		public InstanceEntry(String instanceId, Instance instance, GpuSpec... gpus){
			this(instanceId, instance, System.currentTimeMillis(), gpus);
		}
	}

	static record GpuAllocation(
		InstanceEntry instanceEntry,
		int usedMemoryMB
	){}

	@Data
	@NoArgsConstructor
	public static class Gpu{
		interface Closeable extends AutoCloseable{
			void close();
		}
		public class GpuLock implements Closeable{
			@Override
			public void close() {
				Gpu.this.unlock();
			}
			public int gpuId(){
				return Gpu.this.getSpec().getId();
			}
		}
		@JsonIgnore
		private ReentrantLock lock = new ReentrantLock();
		private GpuSpec spec;
		private int availableMemoryMB;
		private List<GpuAllocation> allocations = new ArrayList<>();
		public Gpu(GpuSpec spec){
			this.spec = spec;
			this.availableMemoryMB = spec.getMemoryMB();
		}
		public GpuLock lock(){
			lock.lock();
			return new GpuLock();
		}
		public void unlock(){
			lock.unlock();
		}
		public boolean releaseInstances() throws InterruptedException{
			try{
				var released = allocations.size() > 0;
				if(released){
					allocations.forEach(
						Functions.<GpuAllocation, InterruptedException>soften(e-> e.instanceEntry().instance().terminateAndWait()));
					allocations.clear();
					availableMemoryMB = spec.getMemoryMB();
				}
				return released;
			} catch(SoftenedException e){
				if(e.getCause() instanceof InterruptedException){
					throw (InterruptedException)e.getCause();
				}
				throw e;
			}
		}
		public boolean releaseInstance(String instanceId) throws InterruptedException{
			var it = allocations.iterator();
			var released = false;
			while(it.hasNext()){
				var a = it.next();
				var ie = a.instanceEntry();
				if(ie.instanceId().equals(instanceId)){
					released = true;
					ie.instance().terminateAndWait();
					var found = false;
					for(var g : ie.gpus()){
						if(g.getId() == spec.getId()){
							availableMemoryMB += g.getMemoryMB();
							found = true;
						}
					}
					if(!found){
						throw new IllegalStateException("self gpu was not found when releasing instance.");
					}
					it.remove();
				}
			}
			return released;
		}
	}

	public InstancePool(GpuSpec... availableGpus){
		this.gpus = ArrayUtil.map(availableGpus, Gpu.class,
			s->new Gpu(s));
	}

	public Gpu[] getGpus() {
		return gpus;
	}

	public synchronized Gpu.GpuLock acquireAnyGpu() throws InterruptedException{
		var gpu = reserveAnyGpu();
		return gpu.lock();
	}

	public synchronized Instance getInstance(String id, Supplier<Instance> supplier)
	throws InterruptedException {
		return instances.computeIfAbsent(
			id, k->new InstanceEntry(id, supplier.get())).instance();
	}

	public synchronized Instance getInstanceWithAnyGpu(String id, Function<GpuSpec, Instance> supplier)
	throws InterruptedException {
		var ie = instances.get(id);
		if(ie != null) return ie.instance();
		var gpu = reserveAnyGpu();
		ie = new InstanceEntry(id, supplier.apply(gpu.getSpec()), gpu.getSpec());
		gpu.getAllocations().add(new GpuAllocation(ie, gpu.getSpec().getMemoryMB()));
		gpu.setAvailableMemoryMB(0);
		instances.put(id, ie);
		return ie.instance();
	}

	public synchronized Instance getInstanceWithGpus(
		String id, int[] requiredGpuMemoryMBs, Function<GpuSpec[], Instance> supplier)
	throws InterruptedException {
		return unsoft(()->instances.computeIfAbsent(id, Functions.soften(k->{
				var gpus = reserveGpus(requiredGpuMemoryMBs);
				var specs = new GpuSpec[gpus.length];
				for(int i = 0; i < specs.length; i++){
					specs[i] = new GpuSpec(gpus[i].getSpec().getId(), requiredGpuMemoryMBs[i]);
				}
				var ie = new InstanceEntry(id, supplier.apply(specs), specs);
				for(int i = 0; i < specs.length; i++){
					var gpu = gpus[i];
					var spec = specs[i];
					gpu.getAllocations().add(new GpuAllocation(ie, spec.getMemoryMB()));
					gpu.setAvailableMemoryMB(gpu.getAvailableMemoryMB() - spec.getMemoryMB());
				}
				return ie;
			})), InterruptedException.class).instance();
	}

	@SuppressWarnings("unchecked")
	private <U, E extends Throwable> U unsoft(Supplier<U> softenedFunc, Class<E> exceptionClass)
	throws E{
		try{
			return softenedFunc.get();
		} catch(SoftenedException e){
			if(exceptionClass.isAssignableFrom(e.getCause().getClass())){
				throw (E)e.getCause();
			}
			throw e;
		}
	}

	private Gpu reserveAnyGpu() throws InterruptedException{
		Gpu ret = null;
		for(var a : gpus){
			if(a.getAllocations().size() == 0){
				ret = a;
				break;
			}
			if(ret == null){
				ret = a;
			} else if(a.getAllocations().size() < ret.getAllocations().size()){
				ret = a;
			}
		}
		if(ret == null){
			throw new IllegalStateException("no gpu available");
		}
		try(var l = ret.lock()){
			ret.getAllocations().forEach(a->instances.remove(a.instanceEntry().instanceId()));
			ret.releaseInstances();
			return ret;
		}
	}

	private Gpu[] reserveGpus(int[] requiredGpuMemoryMBs) throws InterruptedException{
		var mems = Arrays.copyOf(requiredGpuMemoryMBs, requiredGpuMemoryMBs.length);
		Arrays.sort(mems);
		var ret = new Gpu[requiredGpuMemoryMBs.length];
		var reservedGpus = new LinkedHashSet<Integer>();
		for(var i = mems.length - 1; i >= 0; i--){
			var found = false;
			InstanceEntry oldestInstance = null;
			var mem = mems[i];
			for(var g : gpus){
				if(reservedGpus.contains(g.getSpec().getId())) continue;
				if(mem <= g.getAvailableMemoryMB()){
					found = true;
					// 一時利用しているプロセスがあれば終了するまで待つためにロックを取る
					try(var l = g.lock()){
						ret[i] = g;
						reservedGpus.add(g.getSpec().getId());
					}
					break;
				}
				for(var a : g.allocations){
					var ie = a.instanceEntry();
					if(oldestInstance == null){
						oldestInstance = ie;
					} else if(ie.startedAt() < oldestInstance.startedAt()){
						oldestInstance = ie;
					}
				}
			}
			if(!found){
				if(oldestInstance == null){
					throw new RuntimeException("no available gpus and releasable instance found.");
				}
				for(var g : gpus){
					g.releaseInstance(oldestInstance.instanceId());
				}
				i++;
			}
		}
		return ret;
	}

	public synchronized void terminateInstancesOlderMsThan(
		long thresholdMs)
	throws InterruptedException {
		var it = instances.entrySet().iterator();
		while(it.hasNext()){
			var e = it.next();
			if((System.currentTimeMillis() - e.getValue().startedAt()) >= thresholdMs){
				for(var g : gpus){
					g.releaseInstance(e.getKey());
				}
				it.remove();
			}
		}
	}

	private Gpu[] gpus;
	private Map<String, InstanceEntry> instances = new HashMap<>();
}

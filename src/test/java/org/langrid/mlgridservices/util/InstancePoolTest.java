package org.langrid.mlgridservices.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.langrid.mlgridservices.service.Instance;
import org.langrid.mlgridservices.service.InstancePool;

public class InstancePoolTest {
	@Test
	public void test_getInstanceWithGpus_terminateInstancesOlderMsThan() throws Throwable{
		var createCount = new AtomicInteger();
		var releaseCount = new AtomicInteger();
		var ip = new InstancePool(new GpuSpec(0, 30));
		Function<GpuSpec[], Instance> f = gpus->{
			createCount.incrementAndGet();
			return new Instance(){
				@Override
				public Response exec(String line) throws IOException {
					return new Response(true);
				}
				@Override
				public void terminateAndWait() throws InterruptedException {
					releaseCount.incrementAndGet();
				}
			};
		};
		ip.getInstanceWithGpus("proc1", new int[]{20}, f);
		ip.terminateInstancesOlderMsThan(0);
		ip.getInstanceWithGpus("proc1", new int[]{20}, f);
		ip.terminateInstancesOlderMsThan(0);
		assertEquals(2, createCount.get());
		assertEquals(2, releaseCount.get());
	}

	@Test
	public void test_getInstanceWithGpus_terminateInstancesOlderMsThan2() throws Throwable{
		var createCount = new AtomicInteger();
		var releaseCount = new AtomicInteger();
		var ip = new InstancePool(new GpuSpec(0, 30));
		Function<GpuSpec[], Instance> f = gpus->{
			createCount.incrementAndGet();
			return new Instance(){
				@Override
				public Response exec(String line) throws IOException {
					return new Response(true);
				}
				@Override
				public void terminateAndWait() throws InterruptedException {
					releaseCount.incrementAndGet();
				}
			};
		};
		ip.getInstanceWithGpus("proc1", new int[]{20}, f);
		ip.getInstanceWithGpus("proc2", new int[]{20}, f);
		ip.terminateInstancesOlderMsThan(0);
		assertEquals(2, createCount.get());
		assertEquals(2, releaseCount.get());
	}

	@Test
	public void test_getInstanceWithAnyGpus_terminateInstancesOlderMsThan2() throws Throwable{
		var createCount = new AtomicInteger();
		var releaseCount = new AtomicInteger();
		var ip = new InstancePool(new GpuSpec(0, 30));
		var instance = new Instance(){
			@Override
			public Response exec(String line) throws IOException {
				return new Response(true);
			}
			@Override
			public void terminateAndWait() throws InterruptedException {
				releaseCount.incrementAndGet();
			}
		};
		Function<GpuSpec[], Instance> f1 = gpus->{
			createCount.incrementAndGet();
			return instance;
		};
		Function<GpuSpec, Instance> f2 = gpus->{
			createCount.incrementAndGet();
			return instance;
		};
		ip.getInstanceWithGpus("proc1", new int[]{20}, f1);
		ip.getInstanceWithAnyGpu("proc2", f2);
		ip.getInstanceWithGpus("proc1", new int[]{20}, f1);
		assertEquals(3, createCount.get());
		assertEquals(2, releaseCount.get());
	}
}

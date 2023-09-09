package org.langrid.mlgridservices.util;

import java.util.concurrent.Semaphore;

public class GpuPool {
	public class Gpu implements AutoCloseable{
		public Gpu(int index, int id){
			this.index = index;
			this.id = id;
		}

		public int getId() {
			return id;
		}

		public void release(){
			GpuPool.this.release(this);
		}

		@Override
		public void close(){
			release();
		}

		private int index;
		private int id;
	}

	public GpuPool(int count){
		this.gpuAllocated = new boolean[count];
		this.gpuIds = new int[count];
		for(var i = 0; i < count; i++){
			this.gpuIds[i] = i;
		}
		this.semaphore = new Semaphore(count);
	}

	public GpuPool(int[] gpuIds){
		var count = gpuIds.length;
		this.gpuAllocated = new boolean[count];
		this.gpuIds = gpuIds;
		this.semaphore = new Semaphore(count);
	}

	public int getGpuCount(){
		return gpuAllocated.length;
	}

	public int getAvailableGpuCount(){
		return semaphore.availablePermits();
	}

	public Gpu acquire() throws InterruptedException{
		var gpuIndex = -1;
		semaphore.acquire();
		synchronized(gpuAllocated){
			var gpuCount = gpuAllocated.length;
			for(var i = 1; i < gpuCount; i++){
				var index = (i + lastAllocated) % gpuCount;
				if(!gpuAllocated[index]){
					gpuIndex = index;
					break;
				}
			}
			if(gpuIndex != -1){
				System.out.printf("[GPUPool.acquire] acquire %d%n", gpuIds[gpuIndex]);
				gpuAllocated[gpuIndex] = true;
				lastAllocated = gpuIndex;
			} else{
				semaphore.release();
				throw new RuntimeException("No gpus available while the gpus lock was aquired.");
			}
		}
		return new Gpu(gpuIndex, gpuIds[gpuIndex]);
	}

	private void release(Gpu gpu){
		synchronized(gpuAllocated){
			System.out.printf("[GPUPool.release] release %d%n", gpuIds[gpu.index]);
			gpuAllocated[gpu.index] = false;
		}
		semaphore.release();
	}

	private boolean[] gpuAllocated;
	private int[] gpuIds;
	private Semaphore semaphore;
	private int lastAllocated = -1;
}

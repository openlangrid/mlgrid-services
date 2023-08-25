package org.langrid.mlgridservices.util;

import java.util.concurrent.Semaphore;

public class GPULock implements AutoCloseable{
	public static GPULock acquire() throws InterruptedException{
		return new GPULock();
	}

	private static Semaphore lock = new Semaphore(1);

	public GPULock() throws InterruptedException{
		lock.acquire();
	}

	public void release(){
		lock.release();
	}

	@Override
	public void close() {
		lock.release();
	}
}

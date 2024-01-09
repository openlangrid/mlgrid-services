package org.langrid.mlgridservices.service.impl.test;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.service.ml.interim.TestService;

import jp.go.nict.langrid.service_1_2.ProcessFailedException;

public class TestGpuService implements TestService{
	@Override
	public Object test(Object arg) throws ProcessFailedException {
		System.out.println("[TestGpuService] acquire lock.");
		try(var l = ServiceInvokerContext.getInstancePool().acquireAnyGpu()){
			System.out.println("[TestGpuService] executed with GPU:" + l.gpuId() + ".");
		} catch(InterruptedException e){
		} finally{
			System.out.println("[TestGpuService] release lock.");
		}
		return "result";
	}
}

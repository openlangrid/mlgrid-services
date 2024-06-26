package org.langrid.mlgridservices.service.impl.test;

import java.io.IOException;

import org.langrid.mlgridservices.service.Instance;
import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.service.ml.interim.TestService;

import jp.go.nict.langrid.service_1_2.ProcessFailedException;

public class TestGpuPipelineService implements TestService{
	@Override
	public Object test(Object arg) throws ProcessFailedException {
		try{
			var instance = ServiceInvokerContext.getInstancePool().getInstanceWithAnyGpu(
				TestGpuPipelineService.class.getName(),
				(gpu)->{
					return new Instance(){
						{
							System.out.println("[TestGpuPipelineService] created.");
						}
						public boolean isAlive(){
							return true;
						}
						public Response exec(String input){
							System.out.println("[TestGpuPipelineService] executed.");
							return Response.success();
						}
						public void terminateAndWait(){
							System.out.println("[TestGpuPipelineService] terminated.");
						}
					};
				}
			);
			instance.exec(null);
			return "result";
		} catch(InterruptedException | IOException e){
			throw new ProcessFailedException(e);
		}
	}
}

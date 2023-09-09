package org.langrid.mlgridservices.util;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.langrid.mlgridservices.service.Instance;
import org.langrid.mlgridservices.service.ServiceInvokerContext;

public class GpuPoolTest {
	@Test
	public void test() throws Throwable{
		ServiceInvokerContext.setGpuPool(new GpuPool(3));
		for(var i = 0; i < 10; i++){
			var instanceId = i;
			ServiceInvokerContext.getInstanceWithPooledGpu(
				"instance" + i, id->{
					return new Instance() {
						public boolean exec(String line)
						throws IOException{
							return true;
						}
						public void terminateAndWait()
						throws InterruptedException{
							System.out.println("instance" + instanceId + " with GPU " + id + " is terminated");
						}
					};
				});
		}
	}	
}

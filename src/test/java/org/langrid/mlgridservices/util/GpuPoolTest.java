package org.langrid.mlgridservices.util;

import org.junit.jupiter.api.Test;

public class GpuPoolTest {
	@Test
	public void test() throws Throwable{
/*		ServiceInvokerContext.setGpuPool(new GpuPool(new int[]{1, 2, 3}));
		for(var i = 0; i < 10; i++){
			var instanceId = i;
			ServiceInvokerContext.getInstanceWithPooledGpu(
				"instance" + i, 0, id->{
					System.out.println("instance" + instanceId + " with GPU " + id + " is created.");
					return new Instance() {
						public Response exec(String line)
						throws IOException{
							return new Response(true);
						}
						public void terminateAndWait()
						throws InterruptedException{
							System.out.println("instance" + instanceId + " with GPU " + id + " is terminated.");
						}
					};
				});
		}
*/
	}	
}

package org.langrid.mlgridservices;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@ServletComponentScan
@EnableScheduling
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Scheduled(initialDelay = 10 * 1000, fixedRate = 60 * 1000)
	private void checkInstance(){
		try{
			ServiceInvokerContext.terminateGpuInstanceOlderMsThan(20 * 60 * 1000);
		} catch(InterruptedException e){
		}
	}
}

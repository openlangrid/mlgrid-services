package org.langrid.mlgridservices;

import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.langrid.mlgridservices.service.InstancePool;
import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.java.Log;

@SpringBootApplication
@ServletComponentScan
@EnableScheduling
@Log
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@PostConstruct
	private void init(){
		log.info("Service started with gpus: " + Arrays.toString(config.getAvailableGpus()));
		ServiceInvokerContext.setInstancePool(new InstancePool(config.getAvailableGpus()));
	}
	@Autowired
	private ApplicationYaml config;

	@Scheduled(initialDelay = 10 * 1000, fixedRate = 60 * 1000)
	private void checkInstance(){
		try{
			ServiceInvokerContext.getInstancePool().terminateInstancesOlderMsThan(20 * 60 * 1000);
		} catch(InterruptedException e){
		}
	}
}

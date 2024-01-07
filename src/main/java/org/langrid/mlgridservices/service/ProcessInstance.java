package org.langrid.mlgridservices.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

public class ProcessInstance
implements Instance {
	@Data
	static class GpuInfo{
		private String timestamp;
		@JsonAlias("gpu_name")
		private String gpuName;
		private int index;
		@JsonAlias("memory.total")
		private int totalMemoryMegas;
		@JsonAlias("memory.used")
		private int usedMemoryMegas;
		@JsonAlias("memory.free")
		private int freeMemoryMegas;
		@JsonAlias("utilization.gpu")
		private int gpuUtilizationPercentage;
		@JsonAlias("utilization.memory")
		private int memoryUtilizationPercentage;
	}
	public ProcessInstance(Process process)
	throws IOException{
		this.process = process;
		this.reader = new BufferedReader(new InputStreamReader(
			process.getInputStream(), "UTF-8"));
		this.writer = new PrintWriter(new OutputStreamWriter(
			process.getOutputStream(), "UTF-8"));

		String line = null;
		while((line = reader.readLine()) != null){
			System.out.println("stdout: " + line);
			if(line.equals("ready")) break;
		}
	}

	protected boolean shouldIgnoreStdout(String line){
		return false;
	}

	public synchronized Response exec(String input)
	throws IOException, JsonMappingException {
		writer.println(input);
		writer.flush();
		String response = null;
		while(true){
			response = reader.readLine();
			if(response == null){
				var is = process.getErrorStream();
				var a = is.available();
				var buff = new byte[a];
				is.read(buff);
				System.out.println("stderr: " + new String(buff, "UTF-8"));
				break;
			}
			System.out.println("stdout: " + response);
			if(!shouldIgnoreStdout(response)){
				break;
			}
		}
		do{
			if(response == null) break;
			var vals = response.split(" +", 2);
			if(!vals[0].equals("ok")) break;
			if(vals.length == 1) return new Response(true);
			try{
				var gpuInfos = mapper.readValue(vals[1], GpuInfo[].class);
				ServiceInvokerContext.current().getResponseHeaders().put("gpuInfos", gpuInfos);
				return new Response(true, null, gpuInfos[0].getUsedMemoryMegas());
			} catch(JsonMappingException e){
				e.printStackTrace();
				return Response.fail("invalid response: " + response);
			}
		} while(false);
		return new Response(false);
	}

	public synchronized void terminateAndWait() throws InterruptedException{
		System.out.println("[ProcessInstance] terminate process");
		writer.close();
		if(!process.waitFor(15, TimeUnit.MINUTES)){
			System.out.println("[ProcessInstance] kill process");
			process.destroyForcibly();
		}
	}

	private Process process;
	private PrintWriter writer;
	private BufferedReader reader;
	private ObjectMapper mapper = new ObjectMapper();
}

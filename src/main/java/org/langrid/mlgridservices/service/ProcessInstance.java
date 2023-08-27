package org.langrid.mlgridservices.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonMappingException;

public class ProcessInstance
implements Instance {
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

	public synchronized boolean exec(String input)
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
		return response != null && response.equals("ok");
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
}

package org.langrid.mlgridservices.service;

import java.io.IOException;

import lombok.AllArgsConstructor;
import lombok.Data;

public interface Instance {
	@Data
	@AllArgsConstructor
	public class Response{
		private boolean succeeded;
		private String errorMessage;
		private Integer usedGpuMemory;
		public Response(boolean succeeded){
			this.succeeded = succeeded;
		}
		public static Response success(){
			return new Response(true);
		}
		public static Response fail(String errorMessage){
			return new Response(false, errorMessage, null);
		}
	}
	Response exec(String line)
	throws IOException;

	void terminateAndWait()
	throws InterruptedException;
}

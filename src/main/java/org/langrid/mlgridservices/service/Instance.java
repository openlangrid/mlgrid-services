package org.langrid.mlgridservices.service;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonMappingException;

public interface Instance {
	boolean exec(Object input)
	throws IOException, JsonMappingException;
	
	void terminateAndWait()
	throws InterruptedException;
}

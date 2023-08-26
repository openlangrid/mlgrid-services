package org.langrid.mlgridservices.service;

import java.io.IOException;

public interface Instance {
	boolean exec(String line)
	throws IOException;

	void terminateAndWait()
	throws InterruptedException;
}

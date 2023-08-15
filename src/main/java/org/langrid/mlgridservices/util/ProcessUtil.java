package org.langrid.mlgridservices.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;

public class ProcessUtil {
	public static class ProcessFailedException extends Exception{
		public ProcessFailedException(){
		}
		public ProcessFailedException(String message){
			super(message);
		}
		public ProcessFailedException(String message, Throwable cause){
			super(message, cause);
		}
		public ProcessFailedException(Throwable cause){
			super(cause);
		}
	}

	public static Process run(String cmd, File workDir)
	throws IOException, InterruptedException, ProcessFailedException{
		var pb = new ProcessBuilder("bash", "-c", cmd);
		pb.directory(workDir);
		return pb.start();
	}

	public static void runAndWait(String cmd, File workDir)
	throws IOException, InterruptedException, ProcessFailedException{
		var proc = run(cmd, workDir);
		try {
			proc.waitFor();
			var res = proc.exitValue();
			if(res == 0) {
				return;
			} else {
				var br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
				var lines = new StringBuilder();
				String line = null;
				while ((line = br.readLine()) != null) {
					lines.append(line);
				}
				throw new ProcessFailedException(lines.toString());
			}
		} finally {
			proc.destroy();
		}
	}

	public static void runAndWaitWithInheritingOutput(String cmd, File workDir)
	throws IOException, InterruptedException, ProcessFailedException{
		var proc = runWithInheritingOutput(cmd, workDir);
		try {
			proc.waitFor();
			var res = proc.exitValue();
			if(res == 0) {
				return;
			} else {
				var br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
				var lines = new StringBuilder();
				String line = null;
				while ((line = br.readLine()) != null) {
					lines.append(line);
				}
				throw new ProcessFailedException(lines.toString());
			}
		} finally {
			proc.destroy();
		}
	}

	public static Process runWithInheritingOutput(String cmd, File workDir)
	throws IOException, InterruptedException, ProcessFailedException{
		var pb = new ProcessBuilder("bash", "-c", cmd);
		pb.directory(workDir);
		pb.redirectOutput(Redirect.INHERIT);
		pb.redirectError(Redirect.INHERIT);
		return pb.start();
	}
}

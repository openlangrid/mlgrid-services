package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Function;

import org.langrid.mlgridservices.service.Instance;
import org.langrid.mlgridservices.service.ProcessInstance;
import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.GpuSpec;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.go.nict.langrid.commons.lang.StringUtil;

public abstract class AbstractCmdRepl {
	public AbstractCmdRepl(String basePath) {
		this.basePath = Path.of(basePath);
		this.tempDir = new File(this.basePath.toFile(), "temp");
		tempDir.mkdirs();
	}

	public AbstractCmdRepl(String basePath, String... commands) {
		this(basePath);
		setCommands(commands);
	}

	public void setCommands(String[] commands) {
		this.commands = commands;
		this.instanceKey = "process:" + basePath.toString() + "/" + StringUtil.join(commands, ":");
	}

	public int getRequiredGpus() {
		return requiredGpus;
	}

	public void setRequiredGpus(int requiredGpus){
		this.requiredGpus = requiredGpus;
	}

	public int[] getRequiredGpuMemoryMBs() {
		return requiredGpuMemoryMBs;
	}

	public void setRequiredGpuMemoryMBs(int[] requiredGpuMemoryMBs) {
		this.requiredGpuMemoryMBs = requiredGpuMemoryMBs;
	}

	protected ObjectMapper mapper(){
		return m;
	}

	protected File getTempDir(){
		return tempDir;
	}

	protected File createBaseFile() throws IOException{
		return FileUtil.createUniqueFileWithDateTime(
				tempDir, "", "");
	}

	protected Instance getInstance()
	throws InterruptedException{
		Function<GpuSpec[], Instance> factory = (gpus)->{
				var pb = new ProcessBuilder(commands);
				if(gpus.length > 0){
					String ids = org.langrid.mlgridservices.util.StringUtil.join(gpus, v->""+v.getId(), ",");
					System.out.printf("instance(\"%s\") uses devices %s%n", instanceKey, Arrays.toString(gpus));
					pb.environment().put("NVIDIA_VISIBLE_DEVICES", "" + ids);
				}
				try{
					pb.directory(basePath.toFile());
					pb.redirectError(Redirect.INHERIT);
					return new ProcessInstance(pb.start());
				} catch(IOException e){
					throw new RuntimeException(e);
				}
		};
		var ip = ServiceInvokerContext.getInstancePool();
		if(requiredGpuMemoryMBs.length > 0){
			return ip.getInstanceWithGpus(
				instanceKey, requiredGpuMemoryMBs, factory);
		} else if(requiredGpus == 1){
			return ip.getInstanceWithAnyGpu(
				instanceKey, spec->factory.apply(new GpuSpec[]{spec}));
		} else{
			return ip.getInstance(instanceKey, ()->factory.apply(new GpuSpec[]{}));
		}
	}

	private ObjectMapper m = new ObjectMapper();;

	private Path basePath;
	private String[] commands;
	private int requiredGpus = 0;
	private int[] requiredGpuMemoryMBs = {};

	private String instanceKey;
	private File tempDir;
}

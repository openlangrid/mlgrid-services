package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.langrid.mlgridservices.service.Instance;
import org.langrid.mlgridservices.service.ProcessInstance;
import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.go.nict.langrid.commons.lang.StringUtil;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
		this.instanceKey = "process:" + StringUtil.join(commands, ":");
	}

	public void setRequiredGpuCount(int requiredGpuCount){
		this.requiredGpuCount = requiredGpuCount;
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
		var instance = ServiceInvokerContext.getInstanceWithPooledGpu(
			instanceKey, requiredGpuCount, (gpuIds)->{
				var pb = new ProcessBuilder(commands);
				if(gpuIds.length > 0){
					var ids = org.langrid.mlgridservices.util.StringUtil.join(gpuIds, v->""+v, ",");
					System.out.printf("instance(\"%s\") uses device %d%n", instanceKey, ids);
					pb.environment().put("NVIDIA_VISIBLE_DEVICES", "" + ids);
				}
				try{
					pb.directory(basePath.toFile());
					pb.redirectError(Redirect.INHERIT);
					return new ProcessInstance(pb.start());
				} catch(IOException e){
					throw new RuntimeException(e);
				}
		});
		return instance;
	}

	private ObjectMapper m = new ObjectMapper();;

	private Path basePath;
	private String[] commands;
	private int requiredGpuCount = 1;

	private String instanceKey;
	private File tempDir;
}

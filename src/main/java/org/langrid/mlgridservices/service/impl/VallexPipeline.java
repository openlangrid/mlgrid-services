package org.langrid.mlgridservices.service.impl;

import java.io.IOException;

import org.langrid.mlgridservices.service.ProcessInstance;

public class VallexPipeline extends PipelineExternalCommandTextToSpeech {
	public VallexPipeline(String baseDir, String... commands){
		super(baseDir, commands);
	}

	protected ProcessInstance newProcessInstance(Process process)
	throws IOException{
		return new ProcessInstance(process){
			protected boolean shouldIgnoreStdout(String line){
				return line.startsWith("VALL-E EOS");
			}
		};
	}
}

package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.GPULock;
import org.langrid.mlgridservices.util.ProcessUtil;
import org.langrid.service.ml.interim.TextSimilarityCalculationService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguagePairException;

public class ExternalTextSimilarityCalculationService implements TextSimilarityCalculationService{
    private final File baseDir;
	private String model = "default";
	private String scriptFile = "calc_similarity.py";

	public ExternalTextSimilarityCalculationService(String baseDir, String model){
		this.baseDir = new File(baseDir);
		this.model = model;
	}

	public ExternalTextSimilarityCalculationService(String baseDir){
		this.baseDir = new File(baseDir);
	}

	public void setScriptFile(String scriptFile) {
		this.scriptFile = scriptFile;
	}

	@Override
	public double calculate(String text1, String text1Lang,
		String text2, String text2Lang)
			throws InvalidParameterException, ProcessFailedException, UnsupportedLanguagePairException {
		try(var l = ServiceInvokerContext.acquireGpuLock()){
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var utf8 = StandardCharsets.UTF_8;
			var input1Path = FileUtil.createUniqueFileWithDateTime(tempDir, "", "input1.txt");
			Files.writeString(input1Path.toPath(), text1, utf8);
			var input2Path = FileUtil.createUniqueFileWithDateTime(tempDir, "", "input2.txt");
			Files.writeString(input2Path.toPath(), text2, utf8);
			var outputPath = FileUtil.createUniqueFileWithDateTime(tempDir, "", "output.txt");
			var cmd = String.format(
					"PATH=$PATH:/usr/local/bin " +
					"/usr/local/bin/docker-compose run --rm service " +
					"python3 %s --model \"%s\" " +
						"--input1Path \"%s\" --input1Lang \"%s\" " +
						"--input2Path \"%s\" --input2Lang \"%s\" " +
						"--outputPath \"%s\"",
					scriptFile, model,
					"temp/" + input1Path.getName(), text1Lang,
					"temp/" + input2Path.getName(), text2Lang,
					"temp/" + outputPath.getName());
			System.out.println(cmd);
			ServiceInvokerContext.exec(()->{
				ProcessUtil.runAndWaitWithInheritingOutput(cmd, baseDir);
			}, "execution", "docker-compose");
			return Double.parseDouble(Files.readString(outputPath.toPath()));
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}

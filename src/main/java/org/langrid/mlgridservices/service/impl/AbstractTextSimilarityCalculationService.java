package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.nio.file.Files;

import org.langrid.mlgridservices.service.ServiceInvokerContext;
import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.mlgridservices.util.GPULock;
import org.langrid.mlgridservices.util.ProcessUtil;
import org.langrid.service.ml.interim.TextSimilarityCalculationService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguagePairException;

public class AbstractTextSimilarityCalculationService implements TextSimilarityCalculationService{
    private final File baseDir;
	private String scriptFile = "calc_similarity.py";

	public AbstractTextSimilarityCalculationService(File baseDir){
		this.baseDir = baseDir;
	}

	public void setScriptFile(String scriptFile) {
		this.scriptFile = scriptFile;
	}

	@Override
	public double calculate(String text1, String text1Lang,
		String text2, String text2Lang)
			throws InvalidParameterException, ProcessFailedException, UnsupportedLanguagePairException {
		try(var l = GPULock.acquire()){
			var tempDir = new File(baseDir, "temp");
			tempDir.mkdirs();
			var input1Path = FileUtil.createUniqueFileWithDateTime(tempDir, "", "input1.txt");
			var input2Path = FileUtil.createUniqueFileWithDateTime(tempDir, "", "input2.txt");
			var outputPath = FileUtil.createUniqueFileWithDateTime(tempDir, "", "output.txt");
			var cmd = String.format(
					"PATH=$PATH:/usr/local/bin " +
					"/usr/local/bin/docker-compose run --rm service " +
					"python3 %s --input1Path \"%s\" --input1Lang \"%s\" " +
						"--input2Path \"%s\" --input2Lang \"%s\" " +
						"--outputPath \"%s\"",
					scriptFile,
					"temp/" + input1Path.getName(), text1Lang,
					"temp/" + input2Path.getName(), text2Lang,
					"temp/" + outputPath.getName());
			System.out.println(cmd);
			try(var t = ServiceInvokerContext.startServiceTimer()){
				ProcessUtil.runAndWait(cmd, baseDir);
			}
			return Double.parseDouble(Files.readString(outputPath.toPath()));
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}

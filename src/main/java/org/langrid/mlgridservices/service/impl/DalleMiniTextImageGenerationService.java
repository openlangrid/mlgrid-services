package org.langrid.mlgridservices.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;

import org.langrid.service.ml.TextImageGenerationService;

import jp.go.nict.langrid.commons.io.FileUtil;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class DalleMiniTextImageGenerationService implements TextImageGenerationService{
    private File baseDir = new File("./procs/text_image_generation_dalle_mini");
    @Override
    public byte[] generate(String language, String text, String imageFormat, int maxResults)
            throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException {
        try {
            var tempDir = new File(baseDir, "temp");
            tempDir.mkdirs();
            var temp = FileUtil.createUniqueFile(tempDir, "outimage-", "");
            run(text, "temp/" + temp.getName());
            return Files.readAllBytes(new File(tempDir, temp.getName() + "_0.jpg").toPath());
        } catch(RuntimeException e) {
            throw e;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
	public void run(String text, String outimagePrefix){
		try{
			var cmd = "PATH=$PATH:/usr/local/bin /usr/local/bin/docker-compose " +
                "run --rm dalle-mini python3 run.py " +
				"\"" + text.replaceAll("\"", "\\\"") + "\" " +
                outimagePrefix;
			System.out.println(cmd);
			var pb = new ProcessBuilder("bash", "-c", cmd);
			pb.directory(baseDir);
			var proc = pb.start();
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
					throw new RuntimeException(lines.toString());
				}
			} finally {
				proc.destroy();
			}
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}
}

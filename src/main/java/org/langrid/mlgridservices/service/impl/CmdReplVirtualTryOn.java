package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.langrid.mlgridservices.util.FileUtil;
import org.langrid.service.ml.Image;
import org.langrid.service.ml.interim.VirtualTryOnService;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CmdReplVirtualTryOn
extends AbstractCmdRepl
implements VirtualTryOnService{
	public CmdReplVirtualTryOn(String baseDir) {
		super(baseDir);
	}

	public CmdReplVirtualTryOn(
		String baseDir, String... commands) {
		super(baseDir, commands);
	}

	@Override
	public Image tryOn(byte[] humanImage, String humanImageFormat, String humanPrompt,
			byte[] garmentImage, String garmentImageFormat, String garmentPrompt, String garmentCateogry,
			String promptLanguage)
			throws InvalidParameterException, ProcessFailedException, UnsupportedLanguageException{
		try{
			var baseFile = createBaseFile();
			var inputHumanImageFile = new File(
				baseFile.toString() + ".input_human" + FileUtil.getExtFromFormat(humanImageFormat));
			Files.write(inputHumanImageFile.toPath(), humanImage);
			var inputGarmentImageFile = new File(
				baseFile.toString() + ".input_garment" + FileUtil.getExtFromFormat(garmentImageFormat));
			Files.write(inputGarmentImageFile.toPath(), humanImage);
			var outputFile = baseFile.getName() + ".output.jpg";
			var inputFile = new File(baseFile.toString() + ".input.txt");
			var input = mapper().writeValueAsString(new VirtualTryOnCommandInput(
				getTempDir().getName() + "/" + inputHumanImageFile.getName(), humanPrompt,
				getTempDir().getName() + "/" + inputGarmentImageFile.getName(), garmentPrompt,
				GarmentCategory.valueOf(garmentCateogry), promptLanguage,
				getTempDir().getName() + "/" + outputFile
			));
			Files.writeString(inputFile.toPath(), input, StandardCharsets.UTF_8);

			var ins = getInstance();
			var success = ins.exec(input).isSucceeded();
			if(success){
				var imgFile = new File(getTempDir(), outputFile);
				if(imgFile.exists()){
					return new Image(
						Files.readAllBytes(imgFile.toPath()),
						"image/jpeg"
					);
				}
			}
			return null;
		} catch(IOException e){
			throw new RuntimeException(e);
		} catch(InterruptedException e){
			return null;
		}
	}

	static enum GarmentCategory{
		UPPER_BODY, LOWER_BODY, DRESSES
	}
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	static class VirtualTryOnCommandInput{
		private String humanImagePath;
		private String humanPrompt;
		private String garmentImagePath;
		private String garmentPrompt;
		private GarmentCategory garmentCategory;
		private String promptLanguage;
		private String outputPath;
	}
}

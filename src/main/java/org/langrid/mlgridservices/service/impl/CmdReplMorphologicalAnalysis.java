package org.langrid.mlgridservices.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import jp.go.nict.langrid.commons.util.ArrayUtil;
import jp.go.nict.langrid.service_1_2.AccessLimitExceededException;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.LanguageNotUniquelyDecidedException;
import jp.go.nict.langrid.service_1_2.NoAccessPermissionException;
import jp.go.nict.langrid.service_1_2.NoValidEndpointsException;
import jp.go.nict.langrid.service_1_2.ProcessFailedException;
import jp.go.nict.langrid.service_1_2.ServerBusyException;
import jp.go.nict.langrid.service_1_2.ServiceNotActiveException;
import jp.go.nict.langrid.service_1_2.ServiceNotFoundException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;
import jp.go.nict.langrid.service_1_2.morphologicalanalysis.Morpheme;
import jp.go.nict.langrid.service_1_2.morphologicalanalysis.MorphologicalAnalysisService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CmdReplMorphologicalAnalysis
extends AbstractCmdRepl
implements MorphologicalAnalysisService{
	public CmdReplMorphologicalAnalysis(String basePath) {
		super(basePath);
	}

	public CmdReplMorphologicalAnalysis(String basePath, String... commands) {
		super(basePath, commands);
	}

	@Override
	public Morpheme[] analyze(String language, String text)
			throws AccessLimitExceededException, InvalidParameterException, LanguageNotUniquelyDecidedException,
			NoAccessPermissionException, NoValidEndpointsException, ProcessFailedException, ServerBusyException,
			ServiceNotActiveException, ServiceNotFoundException, UnsupportedLanguageException {
		try{
			var baseFile = createBaseFile();
			var inputTextFile = new File(baseFile.toString() + ".input_text.txt");
			Files.writeString(inputTextFile.toPath(), text, StandardCharsets.UTF_8);
			var inputFile = new File(baseFile.toString() + ".input.txt");
			var outputFile = new File(baseFile.toString() + ".output.txt");
			var input = mapper().writeValueAsString(new MorphologicalAnalysisCommandInput(
				getTempDir().getName() + "/" + inputTextFile.getName(),
				language,
				getTempDir().getName() + "/" + outputFile.getName()
			));
			Files.writeString(inputFile.toPath(), input, StandardCharsets.UTF_8);
			var i = getInstance();
			var success = i.exec(input).isSucceeded();
			if(success && outputFile.exists()){
				var elements = mapper().readValue(
					Files.readString(outputFile.toPath()),
					MorphologicalAnalysisCommandOutputElement[].class);
				return ArrayUtil.map(elements, Morpheme.class, m->new Morpheme(
					m.getWord(), m.getLemma() == "*" ? m.getWord() : m.getLemma(),
					convPos(m.getPos(), m.getPosDetail())
					));
			}
			return null;
		} catch(IOException e){
			throw new RuntimeException(e);
		} catch(InterruptedException e){
			return null;
		}
	}

	private static String convPos(String pos, String posDetail){
		if(pos.equals("名詞")){
			if(posDetail.equals("固有名詞")){
				pos = "noun.proper";
			} else if(posDetail.equals("人名")){
				pos = "noun.proper";
			} else if(posDetail.equals("組織名")){
				pos = "noun.proper";
			} else if(posDetail.equals("地名")){
				pos = "noun.proper";
			} else if(posDetail.equals("一般")){
				pos = "noun.common";
			} else if(posDetail.equals("普通名詞")){
				pos = "noun.common";
			} else if(posDetail.equals("*")){
				pos = "noun";
			} else{
				pos = "noun.other";;
			}
		} else if(pos.equals("動詞")){
			pos = "verb";
		} else if(pos.equals("形容詞")){
			pos = "adjective";
		} else if(pos.equals("副詞")){
			pos = "adverb";
		} else if(pos.equals("*")){
			//unknown
			pos = "unknown";
		} else{
			//other
			pos = "other";
		}
		return pos;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	static class MorphologicalAnalysisCommandInput{
		private String textPath;
		private String textLanguage;
		private String outputPath;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	static class MorphologicalAnalysisCommandOutputElement{
		private String serviceType = MorphologicalAnalysisService.class.getSimpleName();
		private String methodName = "analyze";
		private String word;
		private String pos;
		private String posDetail;
		private String lemma;
	}
}

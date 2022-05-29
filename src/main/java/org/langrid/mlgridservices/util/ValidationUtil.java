package org.langrid.mlgridservices.util;

import java.util.Set;

import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.UnsupportedLanguageException;

public class ValidationUtil {
	public static String getValidLanguage(String paramName, String value, Set<String> supportedLanguages)
	throws UnsupportedLanguageException{
		if(supportedLanguages.contains(value)) return value;
		for(var l : supportedLanguages) {
			if(LanguageUtil.matches(value, l)) return l;
			if(LanguageUtil.matches(l, value)) return l;
		}
		throw new UnsupportedLanguageException(paramName, value);
	}

	public static String getValidAudioFormat(String paramName, String value, Set<String> supportedFormats)
	throws InvalidParameterException{
		if(supportedFormats.contains(value)) return value;
		throw new InvalidParameterException(paramName, "The value must be an one of " + supportedFormats);
	}
}

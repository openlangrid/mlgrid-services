package org.langrid.mlgridservices.util;

public class LanguageUtil {
	public static boolean matches(String input, String target) {
		if(input.equals(target)) return true;
		if(target.startsWith(input + "-")) return true;
		return false;
	}
}

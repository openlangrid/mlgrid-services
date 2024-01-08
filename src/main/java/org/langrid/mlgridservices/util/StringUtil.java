package org.langrid.mlgridservices.util;

import java.util.function.Function;
import java.util.function.IntFunction;

public class StringUtil {
	public static String join(
		int[] elements, IntFunction<String> textizer, String separator)
	{
		return join(elements, textizer, separator, 0, elements.length);
	}

	public static <T> String join(
		T[] elements, Function<T, String> textizer, String separator)
	{
		return join(elements, textizer, separator, 0, elements.length);
	}

	public static String join(
		int[] elements, IntFunction<String> textizer, String separator,
		int begin, int end)
	{
		if(begin < 0) throw new ArrayIndexOutOfBoundsException(begin);
		if(end > elements.length) throw new ArrayIndexOutOfBoundsException(end);
		if(begin > end) throw new ArrayIndexOutOfBoundsException(end - begin);
		if(elements.length == 0) return "";
		if(begin == end) return "";
		var b = new StringBuilder(textizer.apply(elements[0]));
		for(var i = (begin + 1); i < end; i++){
			b.append(separator);
			b.append(textizer.apply(elements[i]));
		}
		return b.toString();
	}

	public static <T> String join(
		T[] elements, Function<T, String> textizer, String separator,
		int begin, int end)
	{
		if(begin < 0) throw new ArrayIndexOutOfBoundsException(begin);
		if(end > elements.length) throw new ArrayIndexOutOfBoundsException(end);
		if(begin > end) throw new ArrayIndexOutOfBoundsException(end - begin);
		if(elements.length == 0) return "";
		if(begin == end) return "";
		var b = new StringBuilder(textizer.apply(elements[0]));
		for(var i = (begin + 1); i < end; i++){
			b.append(separator);
			b.append(textizer.apply(elements[i]));
		}
		return b.toString();
	}
}

package org.langrid.mlgridservices.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtil {
	public static File createUniqueFileWithDateTime(File dir, String prefix, String suffix)
	throws IOException{
		String ds = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date());
		return jp.go.nict.langrid.commons.io.FileUtil.createUniqueFile(
			dir, prefix + ds, suffix);
	}

	public static File createUniqueDirectoryWithDateTime(File dir, String prefix)
	throws IOException{
		String ds = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date());
		return jp.go.nict.langrid.commons.io.FileUtil.createUniqueDirectory(
			dir, prefix + ds);
	}

	public static String getExtFromFormat(String format){
		format = format.toLowerCase();
		if(format.endsWith("jpeg")) return "jpg";
		if(format.endsWith("jpg")) return "jpg";
		if(format.endsWith("png")) return "png";
		if(format.endsWith("wav")) return "wav";
		if(format.endsWith("mp4")) return "mp4";
		var parts = format.split("\\/");
		return parts[parts.length - 1];
	}
}

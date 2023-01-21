package org.langrid.mlgridservices.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtil {
	public static File writeTempFile(File tempDir, byte[] content, String format)
	throws IOException{
		var file = FileUtil.createUniqueFileWithDateTime(
			tempDir, "temp-", "." + FileUtil.getExtFromFormat(format));
		Files.write(file.toPath(), content, StandardOpenOption.CREATE);
		return file;
	}
	public static File createUniqueFileWithDateTime(File dir, String prefix, String suffix)
	throws IOException{
		String ds = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date());
		return jp.go.nict.langrid.commons.io.FileUtil.createUniqueFile(
			dir, prefix + ds, suffix);
	}

	public static String addFileNameSuffix(String path, String suffix){
		var i = path.lastIndexOf('.');
		if(i == -1) return path + suffix;
		return path.substring(0, i) + suffix + path.substring(i);
	}

	public static String changeFileExt(String path, String ext){
		var i = path.lastIndexOf('.');
		if(i == -1) return path + ext;
		return path.substring(0, i) + ext;
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
		if(format.equalsIgnoreCase("text/plain")) return "txt";
		var parts = format.split("\\/");
		return parts[parts.length - 1];
	}
}

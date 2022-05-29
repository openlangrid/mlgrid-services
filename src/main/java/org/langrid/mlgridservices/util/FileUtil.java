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
}

package org.openforis.calc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author S. Ricci
 */
public class Calc {
	
	private static final String INFO_FILE_NAME = "info.properties";
	
	public static final String VERSION;
	
	static {
		Properties info = readInfoProperties();
		VERSION = info.getProperty(Calc.class.getPackage().getName()+".version");
	}

	private static Properties readInfoProperties() {
		InputStream is = null;
		try {
			is = Calc.class.getResourceAsStream(INFO_FILE_NAME);
			Properties info = new Properties();
			info.load(is);
			return info;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			org.apache.commons.io.IOUtils.closeQuietly(is);
		}
	}

}

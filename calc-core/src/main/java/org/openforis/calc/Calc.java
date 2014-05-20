package org.openforis.calc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 */
@Component
public class Calc {
	
	private static final String INFO_FILE_NAME = "info.properties";
	
	private String version;
	
	@PostConstruct
	public void init() {
		Properties info = readInfoProperties();
		version = info.getProperty(Calc.class.getPackage().getName()+".version");
	}

	public String getVersion() {
		return version;
	}
	
	private Properties readInfoProperties() {
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

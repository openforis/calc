package org.openforis.calc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.openforis.calc.system.SystemProperty;
import org.openforis.calc.system.SystemPropertyInitializer;
import org.openforis.calc.system.SystemPropertyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author M. Togna
 * @author S. Ricci
 */
@Component
public class Calc {

	private static final String INFO_FILE_NAME = "info.properties";

	private String version;

	@Value(value = "${saiku.ui.url:/saiku-ui}")
	private String saikuUiUrl;

	@Autowired
	private SystemPropertyManager systemPropertyManager;

	@SuppressWarnings("unused")
	@Autowired
	private SystemPropertyInitializer systemPropertyInitializer;

	@PostConstruct
	public void init() {
		Properties info = readInfoProperties();
		version = info.getProperty(Calc.class.getPackage().getName() + ".version");
	}

	public String getVersion() {
		return version;
	}

	public String getSaikuUiUrl() {
		return saikuUiUrl;
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

	@JsonIgnore
	public File getCalcUserHomeDirectory() {
		String userHome = System.getProperty("user.home");
		File calcUserHome = new File(userHome, "OpenForisCalc");
		if (!calcUserHome.exists()) {
			calcUserHome.mkdirs();
		}

		return calcUserHome;
	}

	@JsonIgnore
	public String getRscriptCommandPath() {
		boolean isWindows = isWindows();

		StringBuilder sb = new StringBuilder();
		if (isWindows) {
			sb.append("\"");
		}

		SystemProperty sp = systemPropertyManager.getSystemPropertyByName(SystemProperty.PROPERTIES.R_EXEC_DIR.toString());
		if (sp == null) {
			throw new RuntimeException("Unable to find R executable files");
		}

		sb.append(sp.getValue());
		sb.append(File.separator);
		sb.append("Rscript");

		if (isWindows) {
			sb.append(".exe\"");
		}

		return sb.toString();
	}

	@JsonIgnore
	public boolean isWindows() {
		String osname = System.getProperty("os.name");
		boolean isWindows = (osname != null && osname.length() >= 7 && osname.substring(0, 7).equalsIgnoreCase("windows"));
		return isWindows;
	}

}

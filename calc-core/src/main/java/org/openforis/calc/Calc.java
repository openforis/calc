package org.openforis.calc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;

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

	private String rscriptCommandPath;

	@PostConstruct
	public void init() {
		Properties info = readInfoProperties();
		version = info.getProperty(Calc.class.getPackage().getName() + ".version");
		
		try {
			initRscriptCommandPath();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("Unable to find Rscript executable file", e);
		}

		if (this.rscriptCommandPath == null) {
			throw new RuntimeException("Unable to find Rscript executable file");
		}
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
		return rscriptCommandPath;
	}

	@JsonIgnore
	public void initRscriptCommandPath() throws IOException, InterruptedException {

		String osname = System.getProperty("os.name");
		if (osname != null && osname.length() >= 7 && osname.substring(0, 7).equalsIgnoreCase("windows")) {
			
			String installPath = null;
			Process rp = Runtime.getRuntime().exec("reg query HKLM\\Software\\R-core\\R");
			StreamHog regHog = new StreamHog(rp.getInputStream());
			rp.waitFor();
			regHog.join();
			installPath = regHog.getInstallPath();

			if (installPath == null) {
				throw new RuntimeException("ERROR: canot find path to R. Make sure reg is available and R was installed with registry settings.");
			}
			rscriptCommandPath = "\""+ installPath + "\\bin\\Rscript.exe\"";
			regHog.interrupt();
			
		} else {
			
				String[] paths = new String[] { "/Library/Frameworks/R.framework/Resources/bin/Rscript",
					"/Library/Frameworks/R.framework/Resources/bin/Rscript", 
					"/usr/local/lib/R/bin/Rscript",
					"/usr/local/lib/R/bin/Rscript", 
					"/usr/lib/R/bin/Rscript", 
					"/usr/lib/R/bin/Rscript",
					"/usr/local/bin/Rscript",
					"/usr/local/bin/Rscript",
					"/sw/bin/Rscript",
					"/sw/bin/Rscript",
					"/usr/common/bin/Rscript",
					"/usr/common/bin/Rscript" ,
					"/opt/bin/Rscript",
					"/opt/bin/Rscript"};
			
			for (String path : paths) {
				if( new File(path).exists() ){
					rscriptCommandPath = path;
					break;
				}
			}
			
		}
		
	}

}

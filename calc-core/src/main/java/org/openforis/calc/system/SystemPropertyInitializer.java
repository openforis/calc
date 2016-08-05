/**
 * 
 */
package org.openforis.calc.system;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author M. Togna
 *
 */
@Component
public class SystemPropertyInitializer {

	@Autowired
	SystemPropertyManager systemPropertyManager;
	
	public SystemPropertyInitializer() {
	}
	
	@PostConstruct
	public void initRscriptCommandPath()  {
		try {
			
			String rExecFolder = null;
			
			String osname = System.getProperty("os.name");
			if (osname != null && osname.length() >= 7 && osname.substring(0, 7).equalsIgnoreCase("windows")) {
				
				String installPath = null;
				Process rp = Runtime.getRuntime().exec("reg query HKLM\\Software\\R-core\\R");
				StreamHog regHog = new StreamHog(rp.getInputStream());
				rp.waitFor();
				regHog.join();
				installPath = regHog.getInstallPath();

//				if (installPath == null) {
//					throw new RuntimeException("ERROR: canot find path to R. Make sure reg is available and R was installed with registry settings.");
//				}
				if( installPath != null ){
					rExecFolder = installPath + File.separator + "bin";
				}
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
					File file = new File(path);
					if( file.exists() ){
						rExecFolder = file.getParent(); 
						break;
					}
				}
				
			}
			
			if(StringUtils.isNotBlank(rExecFolder)){
				systemPropertyManager.save( SystemProperty.PROPERTIES.R_EXEC_DIR.toString() , rExecFolder );
			}
			
		} catch (IOException | InterruptedException e) {
			// Nothing should happen. user must set it manually in system settings section
//			throw new RuntimeException("Unable to find R", e);
		}
	}
}

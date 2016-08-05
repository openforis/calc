/**
 * 
 */
package org.openforis.calc.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author M. Togna
 *
 */
public class StreamHog extends Thread {

	InputStream is;
	// boolean capture;
	String installPath;

	StreamHog(InputStream is) {
		this.is = is;
		// this.capture = capture;
		start();
	}

	public String getInstallPath() {
		return installPath;
	}

	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = br.readLine()) != null) {
				// if (capture) { // we are supposed to capture the output from REG command
				int i = line.indexOf("InstallPath");
				if (i >= 0) {
					String s = line.substring(i + 11).trim();
					int j = s.indexOf("REG_SZ");
					if (j >= 0)
						s = s.substring(j + 6).trim();
					installPath = s;
				}
				// }
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to read outout from process", e);
		}
	}

}

package org.openforis.calc.r;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 
 * @author M. Togna
 * 
 */
class RConnectionProperies extends Properties {

	private static final long serialVersionUID = 1L;

	private static final String SERVER_PROPERTIES_FILE = "rconnection.properties";
	private static final String SERVER_PORT = "server.port";
	private static final String SERVER_URL = "server.url";

	private String url;
	private Integer port;

	RConnectionProperies() {
		InputStream is = RClient.class.getClassLoader().getResourceAsStream(SERVER_PROPERTIES_FILE);
		try {
			load(is);
			is.close();
		} catch (IOException e) {
			throw new RuntimeException("Unable to locate file " + SERVER_PROPERTIES_FILE, e);
		}
	}

	String getUrl() {
		if (url == null) {
			url = getProperty(SERVER_URL);
		}
		return url;
	}

	int getPort() {
		if (port == null) {
			port = Integer.parseInt(getProperty(SERVER_PORT));
		}
		return port;
	}

}
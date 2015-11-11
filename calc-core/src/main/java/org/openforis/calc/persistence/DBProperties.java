/**
 * 
 */
package org.openforis.calc.persistence;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author M. Togna
 *
 */
@Component
public class DBProperties {
	
	@Value("${calc.jdbc.host}")
	private String host;
	
	@Value("${calc.jdbc.db}")
	private String database;
	
	@Value("${calc.jdbc.username}")
	private String user;
	
	@Value("${calc.jdbc.password}")
	private String password;
	
	@Value("${calc.jdbc.port}")
	private int port;
	
	public DBProperties() {
	}

	public String getHost() {
		return host;
	}

	public String getDatabase() {
		return database;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public int getPort() {
		return port;
	}
	

}

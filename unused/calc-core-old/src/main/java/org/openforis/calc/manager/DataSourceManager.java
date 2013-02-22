/**
 * 
 */
package org.openforis.calc.manager;

import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;

/**
 * @author M. Togna
 * 
 */
public class DataSourceManager {

	// TODO
	public List<DataSource> getAll() {
		throw new UnsupportedOperationException("Method not yet implemented");
	}

	// TODO
	public DataSource get() throws Exception {
		Properties properties = new Properties();
		properties.setProperty("maxActive", "20");
		properties.setProperty("initialSize", "5");
		properties.setProperty("maxIdle", "5");
		properties.setProperty("url", "jdbc:postgresql://localhost:5432/calc");
		properties.setProperty("username", "postgres");
		properties.setProperty("password", "postgres");
		properties.setProperty("driverClassName", "org.postgresql.Driver");
		
		DataSource dataSource = BasicDataSourceFactory.createDataSource(properties);
		return dataSource;
	}

	// TODO
	public void save(String username, String password, String url) {
		throw new UnsupportedOperationException("Method not yet implemented");
	}

}

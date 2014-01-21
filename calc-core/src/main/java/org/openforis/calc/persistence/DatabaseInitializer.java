package org.openforis.calc.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * 
 * @author S. Ricci
 *
 */
public class DatabaseInitializer {

	static Logger log = Logger.getLogger(DatabaseInitializer.class);
	
	private String driver;
	private String url;
	private String host;
	private String port;
	private String username;
	private String password;
	
	public DatabaseInitializer(String driver, String url, String host,
			String port, String username, String password) {
		super();
		this.driver = driver;
		this.url = url;
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}
	
	/**
	 * Returns true if it's possible to establish a connection to the database at the specified URL, 
	 * otherwise returns false
	 */
	public boolean isDBInitialized() {
		log.debug(String.format("Check database initialization: driver=%s host=%s port=%s user=%s", driver, host, port, username));
		Connection c = null;
		BasicDataSource ds = null;
		try {
			Class.forName(driver);
			ds = new BasicDataSource();
			ds.setDriverClassName(driver);
			ds.setUrl(url);
			ds.setUsername(username);
			ds.setPassword(password);
			c = ds.getConnection();
			log.debug("Database exists");
			return true;
		} catch (SQLException e) {
			log.debug("Database does not exist");
			//database doesn't exist
			return false;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Database driver not found: " + driver, e);
		} finally {
			closeQuietly(c);
			closeQuietly(ds);
		}
	}
	
	/**
	 * Initializes the database by running the initialization sql script in the proper process
	 */
	public void initDB() throws DatabaseInitializationException {
		if ( "org.postgresql.Driver".equals(driver) ) {
			initPostgresDB();
		} else {
			throw new IllegalArgumentException("Unsupported database driver: " + driver);
		}
	}
	
	/**
	 * Initializes the PostgreSQL database by running the initialization script in the psql process.
	 */
	private void initPostgresDB() throws DatabaseInitializationException {
		String connectionInfo = String.format("user=%s password=%s host=%s port=%s", username, password, host, port);
		
		ProcessBuilder pb = new ProcessBuilder("psql", 
				connectionInfo,
				"-q",
				"-v", "ON_ERROR_STOP=1",
				"-f", "/home/ricci/dev/projects/openforis/calc/calc-core/src/main/resources/org/openforis/calc/db/init.sql"
			);
		try {
			Process p = pb.start();
			p.waitFor();
			InputStream errorStream = p.getErrorStream();
			StringWriter writer = new StringWriter();
			IOUtils.copy(errorStream, writer, "UTF-8");
			String error = writer.toString();
			
			if ( StringUtils.isBlank(error) ) {
				log.info(String.format("Database created successfully: host=%s port=%s url=%s", host, port, url));
			} else {
				throw new DatabaseInitializationException(error);
			}
		} catch (IOException e) {
			throw new DatabaseInitializationException(e);
		} catch (InterruptedException e) {
			throw new DatabaseInitializationException(e);
		}
	}
	
	private void closeQuietly(Connection c) {
		if ( c != null ) {
			try {
				c.close();
			} catch (SQLException e) {
				//do nothing
			}
		}
	}

	private void closeQuietly(BasicDataSource ds) {
		if ( ds != null ) {
			try {
				ds.close();
			} catch (SQLException e) {
				//do nothing
			}
		}
	}
	
	public static class DatabaseInitializationException extends Exception {

		private static final long serialVersionUID = 1L;

		public DatabaseInitializationException() {
			this("Error initializing the database");
		}
		
		public DatabaseInitializationException(String message) {
			super(message);
		}
		
		public DatabaseInitializationException(Throwable cause) {
			super(cause);
		}
		
		public DatabaseInitializationException(String message, Throwable cause) {
			super(message, cause);
		}

	}

}

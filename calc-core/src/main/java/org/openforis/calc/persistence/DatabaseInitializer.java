package org.openforis.calc.persistence;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.log4j.Logger;

/**
 * 
 * @author S. Ricci
 * 
 */
public class DatabaseInitializer {

	private static final String DB_INIT_SCRIPT_TEMPLATE = "org/openforis/calc/db/init_template.sql";

	static Logger log = Logger.getLogger(DatabaseInitializer.class);

	private String driver;
	private String url;
	private String db;
	private String host;
	private String port;
	private String username;
	private String password;
	private String adminUsername;
	private String adminPassword;

	public DatabaseInitializer(String driver, String url, String db, String host, String port, 
			String username, String password, String adminUsername, String adminPassword) {
		super();
		this.driver = driver;
		this.url = url;
		this.db = db;
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.adminUsername = adminUsername;
		this.adminPassword = adminPassword;
	}

	/**
	 * Returns true if it's possible to establish a connection to the database at the specified URL, otherwise returns false
	 */
	public boolean isDBInitialized() {
		log.debug(String.format("Check database initialization: driver=%s host=%s port=%s user=%s", driver, host, port, username));
		Connection c = null;
		try {
			Class.forName(driver);
			c = DriverManager.getConnection(url, username, password);
			log.debug("Database exists");
			return true;
		} catch (SQLException e) {
			log.debug("Database does not exist");
			// database doesn't exist
			return false;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Database driver not found: " + driver, e);
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	/**
	 * Initializes the database by running the initialization sql script in the proper process
	 */
	public void initDB() throws DatabaseInitializationException {
		if ("org.postgresql.Driver".equals(driver)) {
			initPostgresDB();
		} else {
			throw new IllegalArgumentException("Unsupported database driver: " + driver);
		}
	}

	/**
	 * Initializes the PostgreSQL database by running the initialization script in the psql process.
	 */
	private void initPostgresDB() throws DatabaseInitializationException {
		String connectionInfo = String.format("user=%s password=%s host=%s port=%s", adminUsername, adminPassword, host, port);

		try {
			File initScriptFile = createInitScriptFile();
			
			String filePath = initScriptFile.getAbsolutePath();
			
			log.info("Trying to execute init script in: " + filePath);
			
			ProcessBuilder pb = new ProcessBuilder("psql", connectionInfo, "-q", "-v", "ON_ERROR_STOP=1", "-f", filePath);
			
			Process p = pb.start();
			p.waitFor();
			InputStream errorStream = p.getErrorStream();
			StringWriter writer = new StringWriter();
			IOUtils.copy(errorStream, writer, "UTF-8");
			String error = writer.toString();

			if (StringUtils.isBlank(error)) {
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

	private File createInitScriptFile() throws IOException {
		String initScriptTemplate = getInitScriptTemplate();
		
		//substitute parameters in template
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("calc.jdbc.db", db);
		parameters.put("calc.jdbc.username", username);
		parameters.put("calc.jdbc.password", password);
		
		StrSubstitutor sub = new StrSubstitutor(parameters, "%{", "}");
		String initScript = sub.replace(initScriptTemplate);
		
		log.info("Trying to execute db init script:\n" + initScript);
		
		//create temp file with init script
		File initScriptFile = File.createTempFile("openforis_calc", "db_init_script.sql");
		FileUtils.writeStringToFile(initScriptFile, initScript, "UTF-8");
		return initScriptFile;
	}

	private String getInitScriptTemplate() throws IOException {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(DB_INIT_SCRIPT_TEMPLATE);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, "UTF-8");
		return writer.toString();
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

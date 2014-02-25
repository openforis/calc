package org.openforis.calc.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.openforis.calc.psql.Psql;

/**
 * 
 * @author S. Ricci
 * 
 */
public class DatabaseInitializer {

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

	private void initPostgresDB() throws DatabaseInitializationException {
		BasicDataSource dataSource = null;
		try {
			//connect to postgres database
			String postgresDBUrl = String.format("jdbc:postgresql://%s:%s/postgres", host, port);
			dataSource = createDataSource(driver, adminUsername, adminPassword, postgresDBUrl);
			
			Psql psql = new Psql(dataSource);
			if ( ! isRoleDefined(psql, username) ) {
				//create role
				String createRoleSql = String.format("CREATE USER \"%s\" WITH PASSWORD '%s'", username, password);
				psql.execute(createRoleSql);
			}
			//create database
			String createDatabaseSql = String.format("CREATE DATABASE \"%s\" ENCODING 'UTF8' OWNER \"%s\"", db, username);
			psql.execute(createDatabaseSql);
		} catch ( Exception e ) {
			throw new RuntimeException("Error initializing database", e);
		} finally {
			close(dataSource);
		}
		createSchema();
	}

	private BasicDataSource createDataSource(String driver, String username, String password, String url) {
		BasicDataSource dataSource;
		dataSource = new BasicDataSource();
		dataSource.setDriverClassName(driver);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		dataSource.setUrl(url);
		return dataSource;
	}

	protected boolean isRoleDefined(Psql psql, String roleName) {
		Select<Record1<Integer>> select = psql
			.select(DSL.count())
			.from("pg_roles")
			.where(String.format("rolname='%s'", roleName));
		Result<Record1<Integer>> result = select.fetch();
		if ( result != null && ! result.isEmpty() ) {
			Record record = result.get(0);
			Integer count = (Integer) record.getValue(0);
			return count > 0;
		} else {
			return false;
		}
	}
	
	private void createSchema() {
		BasicDataSource dataSource = null;
		try {
			dataSource = createDataSource(driver, username, password, url);
			
			Psql psql = new Psql(dataSource);
			String createSchemaSql = String.format("CREATE SCHEMA \"calc\" AUTHORIZATION \"%s\"", username);
			psql.execute(createSchemaSql);
		} finally {
			close(dataSource);
		}
	}

	private void close(BasicDataSource dataSource) {
		try {
			dataSource.close();
		} catch (SQLException e) {
			log.warn("Error closing connection to postgres database", e);
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

package org.openforis.calc.persistence.jooq;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.jooq.SQLDialect;
import org.jooq.impl.Factory;

/**
 * @author G. Miceli
 */
public class DialectAwareJooqFactory extends Factory {
	
	private static final long serialVersionUID = 1L;
	private static final String POSTGRESQL_DBNAME = "PostgreSQL";

	public DialectAwareJooqFactory(Connection connection) {
		super(connection, getDialect(connection));
	}

	private static SQLDialect getDialect(Connection conn) {
		try {
			DatabaseMetaData metaData = conn.getMetaData();
			String dbName = metaData.getDatabaseProductName();
			if ( dbName.equals(POSTGRESQL_DBNAME) ) {
				return SQLDialect.POSTGRES;
			} else {
				throw new IllegalArgumentException("Unsupported database: "+dbName);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error getting database name", e);
		}
	}
}

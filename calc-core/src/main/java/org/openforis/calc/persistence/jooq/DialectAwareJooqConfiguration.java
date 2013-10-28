package org.openforis.calc.persistence.jooq;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class DialectAwareJooqConfiguration extends DefaultConfiguration {

	private static final long serialVersionUID = 1L;

	public DialectAwareJooqConfiguration(DataSourceConnectionProvider connectionProvider) throws SQLException {
		super();
		set(connectionProvider);
		set(extractDialect());
	}
	
	@SuppressWarnings("deprecation")
	private SQLDialect extractDialect()
			throws SQLException {
		String dbName = extractDbName();
		SQLDialect dialect;
		if ( dbName.equals("PostgreSQL") ) {
			dialect = SQLDialect.POSTGRES;
		} else if ( dbName.equals("Apache Derby") ) {
			dialect = SQLDialect.DERBY;
		} else if ( dbName.equals("SQLite") ) {
			dialect = SQLDialect.SQLITE;
		} else {
			dialect = SQLDialect.SQL99;
		}
		return dialect;
	}

	private String extractDbName() throws SQLException {
		DataSource dataSource = ((DataSourceConnectionProvider) connectionProvider()).dataSource();
		Connection conn = DataSourceUtils.getConnection(dataSource);
		DatabaseMetaData metaData = conn.getMetaData();
		String dbName = metaData.getDatabaseProductName();
		return dbName;
	}
	
}

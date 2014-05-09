/**
 * 
 */
package org.openforis.calc.persistence.liquibase;

import java.sql.Connection;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.integration.spring.SpringLiquibase;

/**
 * @author S. Ricci
 *
 */
public class DatabaseAwareSpringLiquibase extends SpringLiquibase {

	@Override
	protected Database createDatabase(Connection c) throws DatabaseException {
		Database database;
		String dbProductName = getDatabaseProductName();
		if ( SQLiteDatabase.PRODUCT_NAME.equals(dbProductName) ) {
			//schemas are not supported
			DatabaseFactory dbFactory = DatabaseFactory.getInstance();
			JdbcConnection jdbcConnection = new JdbcConnection(c);
			database = dbFactory.findCorrectDatabaseImplementation(jdbcConnection);
		} else {
			database = super.createDatabase(c);
		}
		return database;
	}

}

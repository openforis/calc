package org.openforis.calc.engine;

import javax.sql.DataSource;

import org.openforis.calc.persistence.postgis.Psql;

/**
 * A task which uses the calcuser to perform database operations
 *  
 * @author G. Miceli
 *
 */
public class SqlTask extends Task {

	// Helper methods
	
	private DataSource getDataSource() {
		JobContext ds = getContext();
		return ds.getDataSource();
	}

//	/**
//	 * Uses String.format() to generate SQL using the sqlTemplate
//	 * and passed args
//	 * 
//	 * @param sqlTemplate
//	 * @param args
//	 */
//	protected void executeSql(String sqlTemplate, Object... args) {
//		JdbcTemplate jdbc = getJdbcTemplate();
//		String sql = String.format(sqlTemplate, args);
//		log().debug("Sql: "+sql);
//		jdbc.execute(sql);
//	}
	
	protected Psql psql() {
		DataSource dataSource = getDataSource();
		return new Psql(dataSource);
	}
}

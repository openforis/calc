package org.openforis.calc.engine;

import javax.sql.DataSource;

import org.openforis.calc.persistence.postgis.Psql;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * A task which uses the calcuser to perform database operations
 *  
 * @author G. Miceli
 *
 */
public class SqlTask extends Task {
	private JdbcTemplate jdbcTemplate;
	
	// Helper methods
	
	private DataSource getDataSource() {
		JobContext ds = getContext();
		return ds.getDataSource();
	}

	private JdbcTemplate getJdbcTemplate() {
		if ( jdbcTemplate == null ) {
			DataSource ds = getDataSource();
			this.jdbcTemplate = new JdbcTemplate(ds);
		}
		return jdbcTemplate;
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
		JdbcTemplate jdbc = getJdbcTemplate();
		return new Psql(jdbc);
	}
}

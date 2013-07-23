package org.openforis.calc.engine;

import javax.sql.DataSource;

import org.openforis.calc.nls.Captionable;
import org.openforis.calc.persistence.sql.Sql;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A unit of work in the system.
 * 
 * Tasks are not reusable.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class Task extends Worker implements Captionable {
	@JsonIgnore
	private Job job;
	private JdbcTemplate jdbcTemplate;
	
	public JobContext getContext() {
		return getJob().getContext();
	}

	public Workspace getWorkspace() {
		return getContext().getWorkspace();
	}
	
	public Job getJob() {
		return job;
	}
	
	void setJob(Job job) {
		this.job = job;
	}
	
	// Helper methods
	
	protected DataSource getDataSource() {
		JobContext ds = getContext();
		return ds.getDataSource();
	}
	
	protected JdbcTemplate getJdbcTemplate() {
		if ( jdbcTemplate == null ) {
			DataSource ds = getDataSource();
			this.jdbcTemplate = new JdbcTemplate(ds);
		}
		return jdbcTemplate;
	}

	protected void executeSql(String sqlTemplate, Object... args) {
		JdbcTemplate jdbc = getJdbcTemplate();
		String sql = String.format(sqlTemplate, args);
		jdbc.execute(sql);
	}
	
	protected void setSchema(String schema) {
		String sql = Sql.setSchema(schema);
		executeSql(sql);
	}
}

package org.openforis.calc.engine;

import javax.sql.DataSource;

import org.openforis.calc.nls.Captionable;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.OutputSchema;
import org.openforis.calc.schema.RolapSchema;
import org.springframework.beans.factory.annotation.Value;

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
	@Value("${calc.jdbc.username}")
	private String systemUser;

	@JsonIgnore
	private Job job;
	
	public Workspace getWorkspace() {
		return getJob().getWorkspace();
	}
	
	public boolean isDebugMode() {
		return getJob().isDebugMode();
	}

	protected OutputSchema getOutputSchema() {
		return getJob().getOutputSchema();
	}

	protected RolapSchema getRolapSchema() {
		return getJob().getRolapSchema();
	}

	
	public Job getJob() {
		return job;
	}
	
	void setJob(Job job) {
		this.job = job;
	}
	
	// Helpers
	
	protected DataSource getDataSource() {
		return getJob().getDataSource();
	}
	
	protected Psql psql() {
		DataSource dataSource = getDataSource();
		return new Psql(dataSource);
	}
	
	protected String getSystemUser() {
		return systemUser;
	}
}

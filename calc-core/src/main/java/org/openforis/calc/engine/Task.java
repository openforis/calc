package org.openforis.calc.engine;

import javax.sql.DataSource;

import org.openforis.calc.nls.Captionable;
import org.openforis.calc.persistence.postgis.Psql;

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
	
	public Workspace getWorkspace() {
		return getJob().getWorkspace();
	}
	
	public boolean isDebugMode() {
		return getJob().isDebugMode();
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

	// TODO delete; always use schema name in table references 
	protected void setDefaultSchemaSearchPath() {
		Workspace workspace = getWorkspace();
		psql()
			.setSchemaSearchPath(workspace.getOutputSchema(), Psql.PUBLIC)
			.execute();
	}
}

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

	protected Psql psql() {
		DataSource dataSource = getDataSource();
		return new Psql(dataSource);
	}
	
	protected void setDefaultSchemaSearchPath() {
		Workspace workspace = getWorkspace();
		psql()
			.setSchemaSearchPath(workspace.getOutputSchema(), Psql.PUBLIC)
			.execute();
	}
}

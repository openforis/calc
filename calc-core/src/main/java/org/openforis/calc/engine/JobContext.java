package org.openforis.calc.engine;

import javax.sql.DataSource;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public final class JobContext {
	private Workspace workspace;
	private DataSource dataSource;
	
	JobContext(Workspace workspace, DataSource dataSource) {
		this.workspace = workspace;
		this.dataSource = dataSource;
	}

	public Workspace getWorkspace() {
		return this.workspace;
	}

	public DataSource getDataSource() {
		return this.dataSource;
	}
}
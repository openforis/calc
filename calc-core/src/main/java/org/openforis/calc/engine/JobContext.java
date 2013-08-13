package org.openforis.calc.engine;

import javax.sql.DataSource;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public final class JobContext {
	private Workspace workspace;
	private DataSource dataSource;
	private boolean debugMode;
	
	JobContext(Workspace workspace, DataSource dataSource, boolean debugMode) {
		this.workspace = workspace;
		this.dataSource = dataSource;
		this.debugMode = debugMode;
	}

	public Workspace getWorkspace() {
		return this.workspace;
	}

	public DataSource getDataSource() {
		return this.dataSource;
	}
	
	public boolean isDebugMode() {
		return debugMode;
	}
}
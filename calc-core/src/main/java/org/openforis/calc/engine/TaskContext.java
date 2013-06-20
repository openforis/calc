package org.openforis.calc.engine;

import javax.sql.DataSource;

import org.openforis.calc.r.R;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public final class TaskContext {
	private Workspace workspace;
	private DataSource dataSource;
	private R r;
	
	TaskContext(Workspace workspace, DataSource dataSource, R r) {
		this.workspace = workspace;
		this.dataSource = dataSource;
		this.r = r;
	}

	public Workspace getWorkspace() {
		return this.workspace;
	}

	public DataSource getDataSource() {
		return this.dataSource;
	}

	public R getR() {
		return r;
	}
}
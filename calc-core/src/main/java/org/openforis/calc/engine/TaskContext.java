package org.openforis.calc.engine;

import javax.sql.DataSource;

import org.openforis.calc.r.R;
import org.springframework.context.ApplicationContext;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public final class TaskContext {
	private Workspace workspace;
	private DataSource dataSource;
//	private R r;
//	private ApplicationContext appContext;
	
//	TaskContext(Workspace workspace, DataSource dataSource, R r, ApplicationContext appContext) {
	TaskContext(Workspace workspace, DataSource dataSource) {
		this.workspace = workspace;
		this.dataSource = dataSource;
//		this.r = r;
//		this.appContext = appContext;
	}

	public Workspace getWorkspace() {
		return this.workspace;
	}

	public DataSource getDataSource() {
		return this.dataSource;
	}

//	public R getR() {
//		return r;
//	}
//	
//	public ApplicationContext getAppContext() {
//		return appContext;
//	}
	
}
package org.openforis.calc.engine;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.openforis.calc.r.R;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public final class Context {
	private Workspace workspace;
	private DataSource dataSource;
	private R r;
	private Set<UUID> scheduledTasks;
	
	Context(Workspace workspace, DataSource dataSource, R r) {
		this.workspace = workspace;
		this.dataSource = dataSource;
		this.scheduledTasks = new HashSet<UUID>();
		this.r = r;
	}

	public Workspace getWorkspace() {
		return this.workspace;
	}

	public DataSource getDataSource() {
		return this.dataSource;
	}
	
	public boolean isScheduled(Task task) {
		return scheduledTasks.contains(task.getId());
	}
	
	/**
	 * Note: Makes a defensive copy of the Set
	 */
	public void setScheduledTasks(Set<UUID> taskIds) {
		scheduledTasks.clear();
		scheduledTasks.addAll(taskIds);
	}

	public R getR() {
		return r;
	}
}
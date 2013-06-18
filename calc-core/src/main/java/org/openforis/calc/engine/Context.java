package org.openforis.calc.engine;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public final class Context {
	private Workspace workspace;
	private DataSource dataSource;
	private Set<UUID> scheduledTasks;
	
	public Context(Workspace workspace, DataSource dataSource) {
		this.workspace = workspace;
		this.dataSource = dataSource;
		this.scheduledTasks = new HashSet<UUID>();
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
}
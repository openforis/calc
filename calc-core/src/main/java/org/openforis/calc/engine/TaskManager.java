package org.openforis.calc.engine;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Manages execution of tasks and related locking and threads
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
@Component
public class TaskManager {
	@Autowired
	private Executor taskExecutor;
	
	@Autowired
	private WorkspaceManager workspaceManager;
	/**
	 * Executes a task in the background
	 * 
	 * @param task
	 */
	synchronized
	public void start(Task task) throws WorkspaceLockedException {
		Context ctx = task.getContext();
		Workspace ws = ctx.getWorkspace();
		try {
			workspaceManager.lock(ws.getId());
			task.run();
		} finally {
			workspaceManager.unlock(ws.getId());
		}
	}
}

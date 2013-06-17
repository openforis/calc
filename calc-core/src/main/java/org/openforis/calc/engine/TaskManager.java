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
	public void start(Task task) throws WorkspaceLockedException {
		Context ctx = task.getContext();
		Workspace ws = ctx.getWorkspace();
		if ( workspaceManager.isLocked(ws.getId()) ) {
			throw new WorkspaceLockedException();
		}
//		taskExecutor.
		// TODO threading and locking
//		task.run();
	}
}

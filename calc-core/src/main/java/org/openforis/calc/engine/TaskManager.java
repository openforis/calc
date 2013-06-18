package org.openforis.calc.engine;

import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;

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
	private WorkspaceLockManager workspaceLockManager;
	
	/**
	 * Executes a task in the background
	 * 
	 * @param task
	 */
	synchronized
	public void start(final Task task) throws WorkspaceLockedException {
		final Context ctx = task.getContext();
		final Workspace ws = ctx.getWorkspace();
		final Lock lock = workspaceLockManager.lock(ws.getId());
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					task.run();
				} finally {
					lock.unlock();
				}
			}
		});
	}
}

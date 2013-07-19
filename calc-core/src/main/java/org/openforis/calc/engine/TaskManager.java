package org.openforis.calc.engine;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
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
	
	@Autowired 
	private AutowireCapableBeanFactory beanFactory;
	
	public <T extends Task> T createTask(Class<T> type, TaskContext context) {
		try {
			T task = type.newInstance();
			task.setContext(context);
			beanFactory.autowireBean(task);
			return task;
		} catch ( InstantiationException e ) {
			throw new IllegalArgumentException("Invalid task " + type.getClass(), e);
		} catch ( IllegalAccessException e ) {
			throw new IllegalArgumentException("Invalid task " + type.getClass(), e);
		}
	}


	/**
	 * Executes a task in the background
	 * 
	 * @param task
	 */
	synchronized
	public void start(final Task task) throws WorkspaceLockedException {
		final TaskContext ctx = task.getContext();
		final Workspace ws = ctx.getWorkspace();
		final SimpleLock lock = workspaceLockManager.lock(ws.getId());
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

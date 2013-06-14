package org.openforis.calc.engine;

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
	/**
	 * Executes a task in the background
	 * 
	 * @param task
	 */
	public void execute(Task task) {
		// TODO threading and locking
		task.run();
	}
}

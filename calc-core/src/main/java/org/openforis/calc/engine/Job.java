package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Synchronously executes a series of Tasks in order.  Jobs are also Tasks and
 * may be nested accordingly.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class Job extends Task implements Iterable<Task> {
	private int currentTaskIndex;
	private List<Task> tasks;

	protected Job() {
		this.currentTaskIndex = -1;
		this.tasks = new ArrayList<Task>();
	}

	/**
	 * Initializes each contained task in order.
	 */
	public final void init() {
		for (Task task : tasks) {
			task.init();
		}
		super.init();
	}

	@Override
	protected final long countTotalItems() {
		long totalItems = 0;
		for (Task task : tasks) {
			totalItems += task.getTotalItems();
		}
		return totalItems;
	}
	
	/**
	 * Runs each contained task in order.
	 * 
	 * @throws Exception
	 */
	protected final void execute() throws Throwable {
		for (int i = 0; i < tasks.size(); i++) {
			this.currentTaskIndex = i;
			Task task = tasks.get(i);
			task.run();
			if (task.isFailed()) {
				throw task.getException();
			}
		}
		currentTaskIndex = -1;
	}

	public Task getCurrentTask() {
		return currentTaskIndex >= 0 ? tasks.get(currentTaskIndex) : null;
	}

	public Task getTask(int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Throws IllegalStateException if involked after run() is called
	 * 
	 * @param task
	 */
	protected void addTask(Task task) {
		throw new UnsupportedOperationException();
	}

	public int getCurrentTaskIndex() {
		return this.currentTaskIndex;
	}

	public List<Task> tasks() {
		return Collections.unmodifiableList(tasks);
	}

	@Override
	public Iterator<Task> iterator() {
		return tasks().iterator();
	}
}
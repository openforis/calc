package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Synchronously executes a series of Tasks in order.  Jobs are also Tasks and may be nested accordingly.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class Job extends Task implements Iterable<Task> {
	private int currentTaskIndex;
	private ArrayList<Task> tasks;

	protected Job() {
		this.currentTaskIndex = -1;
		this.tasks = new ArrayList<Task>();
	}
	
	/**
	 * Initializes each contained task in order.
	 */
	public final void init() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Runs each contained task in order.
	 */
	public final boolean execute() {
		throw new UnsupportedOperationException();
	}

	public int getCurrentTask() {
		throw new UnsupportedOperationException();
	}

	public Task getTask(int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Throws IllegalStateException if involked after run() is called
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
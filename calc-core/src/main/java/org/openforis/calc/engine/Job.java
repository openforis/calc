package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Synchronously executes a series of Tasks in order.  Jobs are also Tasks and
 * may be nested accordingly.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class Job extends Task implements Iterable<Task> {
	private int currentTaskIndex;
	private List<Task> tasks;
	
//	@Autowired
//	private TaskManager taskManager;
	
	public Job() {
		this.currentTaskIndex = -1;
		this.tasks = new ArrayList<Task>();
	}

	/**
	 * Copies tasks and respective context into Job; all tasks must have the same context 
	 * @param tasks
	 */
	public Job(List<Task> tasks) {
		this.tasks = new ArrayList<Task>(tasks.size());
		for (Task task : tasks) {
			if ( this.getContext() == null ) {
				setContext(task.getContext());
			} else if ( this.getContext() != task.getContext() ) {
				throw new IllegalArgumentException("All tasks must be in same context");
			}
			this.tasks.add(task);
		}
	}

	/**
	 * Initializes each contained task in order. Called after all tasks have been added 
	 * (i.e. not in constructor!)
	 */
	public final void init() {
		for (Task task : tasks) {
			task.init();
		}
		super.init();
	}

//	@Override
//	protected final long countTotalItems() {
//		long totalItems = 0;
//		for (Task task : tasks) {
//			totalItems += task.getTotalItems();
//		}
//		return totalItems;
//	}
	
	/**
	 * Runs each contained task in order.
	 * 
	 * @throws Exception
	 */
	protected final void execute() throws Throwable {
		this.currentTaskIndex = -1;
		for (Task task : tasks) {
			this.currentTaskIndex += 1;
			if ( task.isScheduled() ) {
				if ( task.getContext() != getContext() ) {
					throw new IllegalStateException("Cannot nest tasks in different contexts");
				}
				task.run();
				if ( task.isFailed() ) {
					throw task.getLastException();
				}
			}
		}
		this.currentTaskIndex = -1;
	}

	public Task getCurrentTask() {
		return currentTaskIndex >= 0 ? tasks.get(currentTaskIndex) : null;
	}

	public Task getTask(int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Throws IllegalStateException if invoked after run() is called
	 * 
	 * @param task
	 */
	protected void addTask(Task task) {
		if ( !isPending() ) {
			throw new IllegalStateException("Cannot add tasks to a job once started");
		}
		tasks.add(task);
	}

	public int getCurrentTaskIndex() {
		return this.currentTaskIndex;
	}

	@JsonInclude
	@JsonProperty
	public List<Task> tasks() {
		return Collections.unmodifiableList(tasks);
	}

	@Override
	public Iterator<Task> iterator() {
		return tasks().iterator();
	}	

	/**
	 * Recursive
	 * @return
	 */
	public Set<UUID> getTaskIds() {
		Set<UUID> ids = new HashSet<UUID>();
		gatherTaskIds(ids);
		return ids;
	}

	private void gatherTaskIds(Set<UUID> ids) {
		for (Task task : tasks) {
			ids.add(task.getId());
			if ( task instanceof Job ) {
				Job subjob = (Job) task;
				subjob.gatherTaskIds(ids);
			}
		}		
	}

	public Task getTask(UUID taskId) {
		for (Task task : tasks) {
			if ( task.getId().equals(taskId) ) {
				return task;
			} else if ( task instanceof Job ) {
				Job subjob = (Job) task;
				Task t = subjob.getTask(taskId);
				if ( t != null ) {
					return t;
				}
			}
		}
		return null;
	}
	
	public void setScheduledTasks(Set<UUID> taskIds) {
		for (Task task : tasks) {
			boolean scheduled = taskIds.contains(task.getId());
			task.setScheduled(scheduled);
			if ( task instanceof Job ) {
				Job subjob = (Job) task;
				subjob.setScheduledTasks(taskIds);
			}
		}		
	}
}
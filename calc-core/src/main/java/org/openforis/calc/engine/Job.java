package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Synchronously executes a series of Tasks in order.  Jobs are also Tasks and
 * may be nested accordingly.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Job extends Worker implements Iterable<Task> {
	private JobContext context;
	private int currentTaskIndex;
	private List<Task> tasks;
	
	@Autowired
	TaskManager taskManager;

	public Job() {
		this.currentTaskIndex = -1;
		this.tasks = new ArrayList<Task>();
	}

	/**
	 * Initializes each contained task in order. Called after all tasks have been added 
	 * (i.e. not in constructor!)
	 */
	public final void init() {
		log().debug("Initializing");
		for (Worker task : tasks) {
			task.init();
		}
	}

	@Override
	protected long countTotalItems() {
		long total = 0;
		for (Task task : tasks) {
			total += task.getTotalItems();
		}
		return total;
	}
	
//	@Override
//	protected final long countTotalItems() {
//		long totalItems = 0;
//		for (Task task : tasks) {
//			totalItems += task.getTotalItems();
//		}
//		return totalItems;
//	}

	@Override
	@Transactional
	public synchronized void run() {
		log().debug("Starting job");
		super.run();
		log().debug(String.format("Finished in %.1f sec", getDuration() / 1000f));
	}
	
	/**
	 * Runs each contained task in order.
	 * 
	 * @throws Exception
	 */
	protected final void execute() throws Throwable {
		this.currentTaskIndex = -1;
		for (Task task : tasks) {
			this.currentTaskIndex += 1;
			if ( task.getContext() != getContext() ) {
				throw new IllegalStateException("Cannot nest tasks in different contexts");
			}
			task.run();
			if ( task.isFailed() ) {
				throw task.getLastException();
			}
		}
		this.currentTaskIndex = -1;
	}

	public Worker getCurrentTask() {
		return currentTaskIndex >= 0 ? tasks.get(currentTaskIndex) : null;
	}

//	public Task getTask(int index) {
//		throw new UnsupportedOperationException();
//	}

	/**
	 * Throws IllegalStateException if invoked after run() is called
	 * 
	 * @param task
	 */
	public void addTask(Task task) {
		if ( !isPending() ) {
			throw new IllegalStateException("Cannot add tasks to a job once started");
		}
		task.setJob(this);
		tasks.add(task);
	}

	/**
	 * Adds a task to the Job
	 * @param task The Class of the task we want to add to the Job
	 * @return The added Task instance
	 */
	@SuppressWarnings("unchecked")
	public <T extends Task> T addTask(Class<T> task) {
		Task newTask = taskManager.createTask(task);
		addTask(newTask);
		return (T) newTask;
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

//	/**
//	 * Recursive
//	 * @return
//	 */
//	public Set<UUID> getTaskIds() {
//		Set<UUID> ids = new HashSet<UUID>();
//		gatherTaskIds(ids);
//		return ids;
//	}
//
//	private void gatherTaskIds(Set<UUID> ids) {
//		for (Task task : tasks) {
//			ids.add(task.getId());
//			if ( task instanceof Job ) {
//				Job subjob = (Job) task;
//				subjob.gatherTaskIds(ids);
//			}
//		}		
//	}

	public Worker getTask(UUID taskId) {
		for (Worker task : tasks) {
			if ( task.getId().equals(taskId) ) {
				return task;
			} else if ( task instanceof Job ) {
				Job subjob = (Job) task;
				Worker t = subjob.getTask(taskId);
				if ( t != null ) {
					return t;
				}
			}
		}
		return null;
	}
	
	public JobContext getContext() {
		return context;
	}
	
	void setContext(JobContext context) {
		this.context = context;
	}
}
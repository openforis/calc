package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.openforis.calc.rdb.OutputSchema;
import org.openforis.calc.rolap.RolapSchema;
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
public class Job extends Worker implements Iterable<Task> {
	private int currentTaskIndex;
	private List<Task> tasks;
	private Workspace workspace;
	private boolean debugMode;
	private DataSource dataSource;
	private OutputSchema outputSchema;
	private RolapSchema rolapSchema;
	
	Job(Workspace workspace, boolean debugMode, DataSource dataSource, OutputSchema outputSchema, RolapSchema rolapSchema) {
		this.currentTaskIndex = -1;
		this.tasks = new ArrayList<Task>();
		this.workspace = workspace;
		this.debugMode = debugMode;
		this.dataSource = dataSource;
		this.outputSchema = outputSchema;
		this.rolapSchema = rolapSchema;
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


	public void addTasks(List<Task> tasks) {
		for (Task task : tasks) {
			addTask(task);
		}
	}

//	/**
//	 * Adds a task to the Job
//	 * @param task The Class of the task we want to add to the Job
//	 * @return The added Task instance
//	 */
//	@SuppressWarnings("unchecked")
//	public <T extends Task> T addTask(Class<T> task) {
//		Task newTask = taskManager.createTask(task);
//		addTask(newTask);
//		return (T) newTask;
//	}

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

	public Worker getTask(UUID taskId) {
		for (Worker task : tasks) {
			if ( task.getId().equals(taskId) ) {
				return task;
			}
		}
		return null;
	}

	public Workspace getWorkspace() {
		return this.workspace;
	}
	
	public boolean isDebugMode() {
		return debugMode;
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}
	
	public RolapSchema getRolapSchema() {
		return rolapSchema;
	}
	
	public OutputSchema getOutputSchema() {
		return outputSchema;
	}
}
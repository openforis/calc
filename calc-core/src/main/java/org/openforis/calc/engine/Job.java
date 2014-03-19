package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.openforis.calc.r.R;
import org.openforis.calc.r.REnvironment;
import org.openforis.calc.r.RException;
import org.openforis.calc.schema.InputSchema;
import org.openforis.calc.schema.OutputSchema;
import org.openforis.calc.schema.RolapSchema;
import org.openforis.calc.schema.Schemas;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Synchronously executes a series of Tasks in order.  Jobs are also Tasks and may be nested accordingly.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public class Job extends Worker implements Iterable<Task> {
	private int currentTaskIndex;
	private List<Task> tasks;
	@JsonIgnore
	private Workspace workspace;
	@JsonIgnore
	private boolean debugMode;
	@JsonIgnore
	private DataSource dataSource;
	@JsonIgnore
	private Schemas schemas;

	@Autowired
	@JsonIgnore
	R r;

	// @JsonIgnore
	// protected REnvironment rEnvironment;

	protected Job(Workspace workspace, DataSource dataSource) {
		this.currentTaskIndex = -1;
		this.tasks = new ArrayList<Task>();
		this.workspace = workspace;
		this.debugMode = false;
		this.dataSource = dataSource;
	}

	protected void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

	protected void setSchemas(Schemas schemas) {
		this.schemas = schemas;
	}

	public Schemas getSchemas() {
		return schemas;
	}

	/**
	 * Initializes each contained task in order. Called after all tasks have been added (i.e. not in constructor!)
	 */
	public void init() {
		super.init();
		log().debug("Initializing");
		// for (Worker task : tasks) {
		// task.init();
		// }
	}

	@Override
	protected long countTotalItems() {
		long total = 0;
		for (Task task : tasks) {
			total += task.getTotalItems();
		}
		return total;
	}

	@Override
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
			task.init();
			task.run();
			if (task.isFailed()) {
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
		if (!isPending()) {
			throw new IllegalStateException("Cannot add tasks to a job once started");
		}
		task.setJob(this);
		tasks.add(task);
	}

	public <T extends Collection<? extends Task>> void addTasks(T tasks) {
		for (Task task : tasks) {
			addTask(task);
		}
	}

	// /**
	// * Adds a task to the Job
	// * @param task The Class of the task we want to add to the Job
	// * @return The added Task instance
	// */
	// @SuppressWarnings("unchecked")
	// public <T extends Task> T addTask(Class<T> task) {
	// Task newTask = taskManager.createTask(task);
	// addTask(newTask);
	// return (T) newTask;
	// }

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
			if (task.getId().equals(taskId)) {
				return task;
			}
		}
		return null;
	}

	public Workspace getWorkspace() {
		return this.workspace;
	}

	protected void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	@JsonIgnore
	public RolapSchema getRolapSchema() {
		return schemas.getRolapSchema();
	}

	@JsonIgnore
	public OutputSchema getOutputSchema() {
		return schemas.getOutputSchema();
	}

	@JsonIgnore
	public InputSchema getInputSchema() {
		return schemas.getInputSchema();
	}

	protected REnvironment newREnvironment() {
		try {
			return r.newEnvironment();
		} catch (RException e) {
			throw new IllegalStateException("Unable to create new r environment", e);
		}
	}

}
package org.openforis.calc.engine;

import org.openforis.calc.nls.Captionable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A unit of work in the system.
 * 
 * Tasks are not reusable.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class Task implements Captionable, Runnable {
	private Context context;
	private Status status;
	private long startTime;
	private long endTime;
	private long itemsProcessed;
	private long itemsSkipped;
	private long totalItems;
	private Throwable exception;
	private ParameterMap parameters;
	private Logger logger;
	
	public enum Status {
		NOT_STARTED, RUNNING, COMPLETE, FAILED, ABORTED;
	}

	protected Task() {
		this.logger = LoggerFactory.getLogger(getClass());
	}
	
	public static <T extends Task> T createTask(Class<T> type, Context context, ParameterMap parameters) {
		try {
			T task = type.newInstance();
			task.context = context;
			task.parameters = parameters;
			return task;
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("Invalid task "+type.getClass(), e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Invalid task "+type.getClass(), e);
		}
	}

	protected Task(Context context, ParameterMap parameters) {
		this.context = context;
		this.parameters = parameters;
	}

	/**
	 * Executed before run() to count the totalItems and perform other quick checks.
	 */
//	public abstract void init();

	// TODO Temporary: replace with abstract
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public final void run() {
		try {
			boolean success = execute();
			this.status = success ? Status.COMPLETE : Status.FAILED;
		} catch (Throwable t) {
			this.status = Status.FAILED;
			this.exception = t; 
		}
		this.endTime = System.currentTimeMillis();
	}

	protected boolean execute() throws Exception {
		// TODO make abstract
		throw new UnsupportedOperationException();
	}

	public long getDuration() {
		throw new UnsupportedOperationException();
	}

	public boolean isRunning() {
		throw new UnsupportedOperationException();
	}

	public boolean isFailed() {
		throw new UnsupportedOperationException();
	}

	public boolean isAborted() {
		throw new UnsupportedOperationException();
	}

	public boolean isFinished() {
		throw new UnsupportedOperationException();
	}

	public ParameterMap getParameters() {
		return this.parameters;
	}

	public Context getContext() {
		return this.context;
	}

	public Task.Status getStatus() {
		return this.status;
	}

	public long getStartTime() {
		return this.startTime;
	}

	public long getEndTime() {
		return this.endTime;
	}

	public long getItemsProcessed() {
		return this.itemsProcessed;
	}

	public long getItemsSkipped() {
		return this.itemsSkipped;
	}

	public long getTotalItems() {
		return this.totalItems;
	}

	public Throwable getException() {
		return this.exception;
	}
	
	protected Logger log() {
		return this.logger;
	}
}
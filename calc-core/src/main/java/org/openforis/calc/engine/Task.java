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
		NOT_STARTED, RUNNING, FINISHED, FAILED, ABORTED;
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

	/**
	 * Executed before run() to count the totalItems and perform other quick checks.
	 */

	public void init() {
		this.startTime = -1;
		this.endTime = -1;
		this.itemsProcessed = 0;
		this.itemsSkipped = 0;
		this.exception = null;
		this.totalItems = countTotalItems();
	}

	protected long countTotalItems() {
		// TODO replace with abstract 
		return 1;
	}

	@Override
	public final void run() {
		try {
			this.startTime = System.currentTimeMillis();
			execute();
			this.status = Status.FINISHED;
		} catch (Throwable t) {
			this.status = Status.FAILED;
			this.exception = t; 
		} finally {
			this.endTime = System.currentTimeMillis();
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	protected void execute() throws Throwable {
		// TODO make abstract
		throw new UnsupportedOperationException();
	}

	public final long getDuration() {
		return status == Status.NOT_STARTED ? -1 : endTime - startTime;
	}

	public final boolean isRunning() {
		return status == Status.RUNNING;
	}

	public final boolean isFailed() {
		return status == Status.FAILED;
	}

	public final boolean isAborted() {
		return status == Status.ABORTED;
	}

	public final boolean isFinished() {
		return status == Status.FINISHED;
	}

	public final Context getContext() {
		return this.context;
	}

	public final Task.Status getStatus() {
		return this.status;
	}

	public final long getStartTime() {
		return this.startTime;
	}

	public final long getEndTime() {
		return this.endTime;
	}

	public long getItemsProcessed() {
		return this.itemsProcessed;
	}

	public long getItemsSkipped() {
		return this.itemsSkipped;
	}

	public final long getTotalItems() {
		return this.totalItems;
	}

	public final Throwable getException() {
		return this.exception;
	}
	
	protected final Logger log() {
		return this.logger;
	}

	protected final ParameterMap getParameters() {
		return parameters;
	}
}
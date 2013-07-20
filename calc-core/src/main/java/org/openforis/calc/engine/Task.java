package org.openforis.calc.engine;

import java.util.UUID;

import org.openforis.calc.nls.Captionable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A unit of work in the system.
 * 
 * Tasks are not reusable.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class Task implements Captionable {
	@JsonIgnore
	private TaskContext context;
	private Status status;
	private UUID id;
	private long startTime;
	private long endTime;
	private long itemsProcessed;
	private long itemsSkipped;
	private long totalItems;
	// @JsonIgnore
	private Throwable lastException;
	@JsonIgnore
	private Logger logger;
	
	public enum Status {
		PENDING, RUNNING, COMPLETED, FAILED, ABORTED;
	}

	/**
	 * @param context
	 */
	protected Task() {
		reset();
		this.logger = LoggerFactory.getLogger(getClass());
		this.id = UUID.randomUUID();
	}

	/**
	 * Executed before run() to count the totalItems and perform other quick checks.
	 */
	synchronized public void reset() {
		this.status = Status.PENDING;
		this.startTime = -1;
		this.endTime = -1;
		this.itemsProcessed = 0;
		this.itemsSkipped = 0;
		this.lastException = null;
	}

	synchronized public void init() {
		this.totalItems = countTotalItems();
	}

	protected long countTotalItems() {
		// TODO replace with abstract
		return 1;
	}

	synchronized public final void run() {
		if ( context == null ) {
			throw new IllegalStateException("Context not set");
		}
		try {
			reset();
			this.startTime = System.currentTimeMillis();
			execute();
			this.status = Status.COMPLETED;
		} catch ( Throwable t ) {
			this.status = Status.FAILED;
			this.lastException = t;
			logger.warn("Task failed");
			t.printStackTrace();
		} finally {
			this.endTime = System.currentTimeMillis();
			notifyAll();
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
		return status == Status.PENDING ? -1 : endTime - startTime;
	}

	public final boolean isPending() {
		return status == Status.PENDING;
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

	public final boolean isCompleted() {
		return status == Status.COMPLETED;
	}

	/**
	 * If task was run and finished, aborted or failed
	 * 
	 * @return
	 */
	public final boolean isEnded() {
		return status != Status.PENDING && status != Status.RUNNING;
	}

	public final TaskContext getContext() {
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

	public final Throwable getLastException() {
		return this.lastException;
	}

	public UUID getId() {
		return id;
	}

	protected final Logger log() {
		return this.logger;
	}

	void setContext(TaskContext context) {
		this.context = context;
	}
	
	public synchronized boolean waitFor(int timeoutMillis) {
		long start = System.currentTimeMillis();
		while (!isEnded() && System.currentTimeMillis() - start < timeoutMillis) {
			try {
				wait(timeoutMillis);
			} catch (InterruptedException e) {
			}
		}
		return isCompleted();
	}
}

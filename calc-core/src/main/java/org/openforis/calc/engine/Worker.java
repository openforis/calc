package org.openforis.calc.engine;

import java.util.UUID;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openforis.calc.r.RScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Base class for asynchronous
 * 
 * @author G. Miceli
 * 
 */
public abstract class Worker {

	private Status status;
	private UUID id;
	private long startTime;
	private long endTime;
	private long itemsProcessed;
	private long itemsSkipped;
	private long totalItems;
	//  deserializing it into json might cause problems
	@JsonIgnore
	private Throwable lastException;
	// TODO can use 'transient' Java keyword instead?
	@JsonIgnore
	private Logger logger;
	
	private String errorStackTrace;

	public enum Status {
		PENDING, RUNNING, COMPLETED, FAILED, ABORTED;
	}

	public Worker() {
		this.setStatus(Status.PENDING);
		this.startTime = -1;
		this.endTime = -1;
		this.itemsProcessed = 0;
		this.itemsSkipped = 0;
		this.lastException = null;
		this.logger = LoggerFactory.getLogger(getClass());
		this.id = UUID.randomUUID();
	}

	synchronized public void init() {
		this.totalItems = countTotalItems();
	}

	// TODO
	// protected abstract long countTotalItems();
	protected long countTotalItems() {
		return -1;
	};

	protected abstract void execute() throws Throwable;

	public String getName() {
		return getClass().getSimpleName();
	}

	public synchronized void run() {
		if ( !isPending() ) {
			throw new IllegalStateException("Already run");
		}
		try {
			this.startTime = System.currentTimeMillis();

			this.setStatus( Status.RUNNING );
			execute();
			this.setStatus( Status.COMPLETED );
			
		} catch (Throwable t) {
			this.errorStackTrace = ExceptionUtils.getStackTrace(t);
			this.setStatus( Status.FAILED );
			this.lastException = t;
			logger.warn("Task failed");
			log().error("Error while executing task", t);
		} finally {
			this.endTime = System.currentTimeMillis();
			notifyAll();
		}
	}

	public final long getDuration() {
		switch (getStatus()) {
		case PENDING:
			return -1;
		case RUNNING:
			return System.currentTimeMillis() - startTime;
		default:
			return endTime - startTime;
		}
	}

	public final boolean isPending() {
		return getStatus() == Status.PENDING;
	}

	public final boolean isRunning() {
		return getStatus() == Status.RUNNING;
	}

	public final boolean isFailed() {
		return getStatus() == Status.FAILED;
	}

	public final boolean isAborted() {
		return getStatus() == Status.ABORTED;
	}

	public final boolean isCompleted() {
		return getStatus() == Status.COMPLETED;
	}

	/**
	 * If task was run and finished, aborted or failed
	 * 
	 * @return
	 */
	public final boolean isEnded() {
		return getStatus() != Status.PENDING && getStatus() != Status.RUNNING;
	}

	public final Task.Status getStatus() {
		return this.status;
	}
	
	void setStatus(Status status) {
		this.status = status;
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

	public void setItemsProcessed(long itemsProcessed) {
		this.itemsProcessed = itemsProcessed;
	}

	public long incrementItemsProcessed() {
		return ++this.itemsProcessed;
	}

	public long incrementItemsSkipped() {
		return ++this.itemsSkipped;
	}

	public long getItemsRemaining() {
		return getTotalItems() - (getItemsProcessed() + getItemsSkipped());
	}

	public UUID getId() {
		return id;
	}

	public String getErrorStackTrace() {
		return errorStackTrace;
	}
	
	protected final Logger log() {
		return this.logger;
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

	// returns a new r script. left here now since this is the common super
	// class between calcjob and calcrscript
	protected RScript r() {
		return new RScript();
	}

}
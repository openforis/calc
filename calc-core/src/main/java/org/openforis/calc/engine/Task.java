package org.openforis.calc.engine;

import org.openforis.calc.nls.Captionable;
import org.openforis.calc.workspace.Workspace;
import javax.sql.DataSource;

/**
 * A unit of work in the system.
 * 
 * Tasks are not reusable.
 */
public abstract class Task implements Captionable {
	private org.openforis.calc.engine.Task.Context context;
	private org.openforis.calc.engine.Task.Status status;
	private long startTime;
	private long endTime;
	private long itemsProcessed;
	private long itemsSkipped;
	private long totalItems;
	private TaskFailureException exception;
	private Parameters parameters;

	public static <T extends Task> T createTask(Class<T> type, org.openforis.calc.engine.Task.Context context) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Executed before run() to count the totalItems and perform other quick checks.
	 */
//	public abstract void init();

	// TODO Temporary: replace with abstract
	public void init() {
		// TODO Auto-generated method stub
		
	}


//	public abstract void run();
	// TODO Temporary: replace with abstract
	public void run() {
		// TODO Auto-generated method stub
		
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

	public Parameters getParameters() {
		return this.parameters;
	}

	public org.openforis.calc.engine.Task.Context getContext() {
		return this.context;
	}

	public org.openforis.calc.engine.Task.Status getStatus() {
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

	public TaskFailureException getException() {
		return this.exception;
	}
	public static final class Context {
		private Workspace workspace;
		private DataSource dataSource;

		public Workspace getWorkspace() {
			return this.workspace;
		}

		public DataSource getDataSource() {
			return this.dataSource;
		}
	}
	public enum Status {
		NOT_STARTED, RUNNING, COMPLETE, FAILED, ABORTED;
	}
}